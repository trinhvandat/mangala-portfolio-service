package org.mangala.portfolio.holdings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingsAggregationServiceImpl implements HoldingsAggregationService {

    private static final String PORTFOLIO_CACHE_PREFIX = "portfolio:summary:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final Duration DEBOUNCE_INTERVAL = Duration.ofSeconds(10);

    private final RedissonClient redissonClient;
    private final PortfolioWalletRepository portfolioWalletRepository;
    private final PortfolioRepository portfolioRepository;
    private final PriceCacheService priceCacheService;

    // In-memory debounce tracking
    private final Map<UUID, Instant> lastRecalculation = new ConcurrentHashMap<>();

    @Override
    public void processBalanceUpdate(BalanceUpdateEvent event) {
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

        // This would need to fetch balances from wallet service or cached balance data
        // For now, return an empty summary structure
        return PortfolioSummary.builder()
                .portfolioId(portfolioId)
                .totalValueUsd(BigDecimal.ZERO)
                .change24hUsd(BigDecimal.ZERO)
                .change24hPercent(BigDecimal.ZERO)
                .holdings(new ArrayList<>())
                .chainBreakdown(new HashMap<>())
                .lastUpdated(Instant.now())
                .build();
    }

    @Override
    public void invalidateCache(UUID portfolioId) {
        String cacheKey = PORTFOLIO_CACHE_PREFIX + portfolioId;
        redissonClient.getBucket(cacheKey).delete();
    }
}
