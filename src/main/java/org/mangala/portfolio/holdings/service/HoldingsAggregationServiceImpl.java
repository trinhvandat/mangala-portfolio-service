package org.mangala.portfolio.holdings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mangala.portfolio.holdings.domain.AggregatedHolding;
import org.mangala.portfolio.holdings.domain.PortfolioSummary;
import org.mangala.portfolio.holdings.event.BalanceUpdateEvent;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioWalletRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.domain.PortfolioWalletEntity;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingsAggregationServiceImpl implements HoldingsAggregationService {

    private static final String PORTFOLIO_CACHE_PREFIX = "portfolio:summary:";
    private static final String WALLET_BALANCE_PREFIX = "wallet:balance:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final Duration BALANCE_TTL = Duration.ofMinutes(10);
    private static final Duration DEBOUNCE_INTERVAL = Duration.ofSeconds(10);

    private final RedissonClient redissonClient;
    private final PortfolioWalletRepository portfolioWalletRepository;
    private final PortfolioRepository portfolioRepository;
    private final PriceCacheService priceCacheService;

    // In-memory debounce tracking
    private final Map<UUID, Instant> lastRecalculation = new ConcurrentHashMap<>();

    @Override
    public void processBalanceUpdate(BalanceUpdateEvent event) {
        // Store wallet balance in Redis for later aggregation
        storeWalletBalance(event);

        // Find all portfolios containing this wallet
        List<PortfolioWalletEntity> portfolioWallets = portfolioWalletRepository.findByWalletId(event.getWalletId());

        for (PortfolioWalletEntity pw : portfolioWallets) {
            UUID portfolioId = pw.getPortfolioId();

            // Debounce: skip if recalculated within the last 10 seconds
            Instant lastCalc = lastRecalculation.get(portfolioId);
            if (lastCalc != null && Duration.between(lastCalc, Instant.now()).compareTo(DEBOUNCE_INTERVAL) < 0) {
                log.debug("Skipping recalculation for portfolio {} (debounced)", portfolioId);
                continue;
            }

            // Invalidate cache and trigger recalculation
            invalidateCache(portfolioId);
            lastRecalculation.put(portfolioId, Instant.now());
            log.debug("Invalidated cache for portfolio {} due to balance update", portfolioId);
        }
    }

    /**
     * Store wallet balance data in Redis for later aggregation.
     */
    private void storeWalletBalance(BalanceUpdateEvent event) {
        String key = WALLET_BALANCE_PREFIX + event.getWalletId();
        RBucket<BalanceUpdateEvent> bucket = redissonClient.getBucket(key);
        bucket.set(event, BALANCE_TTL);
        log.debug("Stored balance for wallet {} with {} tokens", event.getWalletId(),
                event.getBalances() != null ? event.getBalances().size() : 0);
    }

    /**
     * Retrieve stored wallet balance from Redis.
     */
    private Optional<BalanceUpdateEvent> getStoredWalletBalance(UUID walletId) {
        String key = WALLET_BALANCE_PREFIX + walletId;
        RBucket<BalanceUpdateEvent> bucket = redissonClient.getBucket(key);
        return Optional.ofNullable(bucket.get());
    }

    @Override
    public PortfolioSummary getPortfolioSummary(UUID portfolioId) {
        String cacheKey = PORTFOLIO_CACHE_PREFIX + portfolioId;
        RBucket<PortfolioSummary> bucket = redissonClient.getBucket(cacheKey);

        PortfolioSummary cached = bucket.get();
        if (cached != null) {
            return cached;
        }

        // Calculate and cache
        PortfolioSummary summary = calculatePortfolioSummary(portfolioId);
        if (summary != null) {
            bucket.set(summary, CACHE_TTL);
        }
        return summary;
    }

    private PortfolioSummary calculatePortfolioSummary(UUID portfolioId) {
        Optional<PortfolioEntity> portfolioOpt = portfolioRepository.findByIdWithWallets(portfolioId);
        if (portfolioOpt.isEmpty()) {
            return null;
        }

        PortfolioEntity portfolio = portfolioOpt.get();
        List<UUID> walletIds = portfolio.getWallets().stream()
                .map(PortfolioWalletEntity::getWalletId)
                .toList();

        // Aggregate balances by symbol across all wallets
        Map<String, TokenAggregation> symbolAggregation = new HashMap<>();
        Map<String, BigDecimal> chainBreakdown = new HashMap<>();

        for (UUID walletId : walletIds) {
            Optional<BalanceUpdateEvent> balanceOpt = getStoredWalletBalance(walletId);
            if (balanceOpt.isEmpty() || balanceOpt.get().getBalances() == null) {
                log.debug("No cached balance for wallet {}", walletId);
                continue;
            }

            BalanceUpdateEvent balance = balanceOpt.get();
            String chain = balance.getChainType() != null ? balance.getChainType() : "UNKNOWN";

            for (BalanceUpdateEvent.TokenBalanceData token : balance.getBalances()) {
                String symbol = token.getSymbol().toUpperCase();
                BigDecimal quantity = token.getBalance();

                if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                // Get price from cache
                BigDecimal priceUsd = priceCacheService.getPriceUsd(symbol);
                BigDecimal valueUsd = quantity.multiply(priceUsd);

                // Aggregate by symbol
                symbolAggregation.computeIfAbsent(symbol, k -> new TokenAggregation(symbol, token.getName()))
                        .add(quantity, valueUsd, chain);

                // Aggregate by chain
                chainBreakdown.merge(chain, valueUsd, BigDecimal::add);
            }
        }

        // Build aggregated holdings list
        List<AggregatedHolding> holdings = new ArrayList<>();
        BigDecimal totalValueUsd = BigDecimal.ZERO;
        BigDecimal totalChange24hUsd = BigDecimal.ZERO;

        for (TokenAggregation agg : symbolAggregation.values()) {
            BigDecimal priceUsd = priceCacheService.getPriceUsd(agg.symbol);
            BigDecimal change24h = priceCacheService.getPrice(agg.symbol)
                    .map(p -> p.getChange24h() != null ? p.getChange24h() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);

            BigDecimal holdingValue = agg.totalQuantity.multiply(priceUsd);
            BigDecimal changeUsd = holdingValue.multiply(change24h).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalValueUsd = totalValueUsd.add(holdingValue);
            totalChange24hUsd = totalChange24hUsd.add(changeUsd);

            holdings.add(AggregatedHolding.builder()
                    .symbol(agg.symbol)
                    .name(agg.name)
                    .totalQuantity(agg.totalQuantity)
                    .priceUsd(priceUsd)
                    .valueUsd(holdingValue)
                    .change24hPercent(change24h)
                    .allocation(BigDecimal.ZERO) // Will calculate after total is known
                    .chains(new ArrayList<>(agg.chains))
                    .build());
        }

        // Calculate allocations
        final BigDecimal finalTotal = totalValueUsd;
        if (finalTotal.compareTo(BigDecimal.ZERO) > 0) {
            holdings.forEach(h -> h.setAllocation(
                    h.getValueUsd().multiply(BigDecimal.valueOf(100))
                            .divide(finalTotal, 2, RoundingMode.HALF_UP)));
        }

        // Sort by value descending
        holdings.sort((a, b) -> b.getValueUsd().compareTo(a.getValueUsd()));

        // Calculate total 24h change percent
        BigDecimal change24hPercent = totalValueUsd.compareTo(BigDecimal.ZERO) > 0
                ? totalChange24hUsd.multiply(BigDecimal.valueOf(100)).divide(totalValueUsd, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return PortfolioSummary.builder()
                .portfolioId(portfolioId)
                .totalValueUsd(totalValueUsd)
                .change24hUsd(totalChange24hUsd)
                .change24hPercent(change24hPercent)
                .holdings(holdings)
                .chainBreakdown(chainBreakdown)
                .lastUpdated(Instant.now())
                .build();
    }

    /**
     * Helper class for aggregating token data across wallets.
     */
    private static class TokenAggregation {
        final String symbol;
        final String name;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalValueUsd = BigDecimal.ZERO;
        Set<String> chains = new HashSet<>();

        TokenAggregation(String symbol, String name) {
            this.symbol = symbol;
            this.name = name;
        }

        void add(BigDecimal quantity, BigDecimal valueUsd, String chain) {
            this.totalQuantity = this.totalQuantity.add(quantity);
            this.totalValueUsd = this.totalValueUsd.add(valueUsd);
            this.chains.add(chain);
        }
    }

    @Override
    public void invalidateCache(UUID portfolioId) {
        String cacheKey = PORTFOLIO_CACHE_PREFIX + portfolioId;
        redissonClient.getBucket(cacheKey).delete();
    }
}
