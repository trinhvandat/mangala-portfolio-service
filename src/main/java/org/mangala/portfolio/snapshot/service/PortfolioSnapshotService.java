package org.mangala.portfolio.snapshot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mangala.portfolio.holdings.domain.AggregatedHolding;
import org.mangala.portfolio.holdings.domain.PortfolioSummary;
import org.mangala.portfolio.holdings.service.HoldingsAggregationService;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.snapshot.adapter.repository.PortfolioSnapshotRepository;
import org.mangala.portfolio.snapshot.domain.HoldingSnapshot;
import org.mangala.portfolio.snapshot.domain.PortfolioSnapshotEntity;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotService {

    private static final String SNAPSHOT_LOCK = "portfolio:snapshot:lock";
    private static final Duration LOCK_TTL = Duration.ofMinutes(30);

    private final PortfolioRepository portfolioRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final HoldingsAggregationService holdingsAggregationService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void createHourlySnapshots() {
        RLock lock = redissonClient.getLock(SNAPSHOT_LOCK);

        try {
            if (!lock.tryLock(0, LOCK_TTL.toMinutes(), TimeUnit.MINUTES)) {
                log.debug("Snapshot job already running on another instance");
                return;
            }

            log.info("Starting hourly portfolio snapshot job");
            Instant snapshotTime = Instant.now().truncatedTo(ChronoUnit.HOURS);

            List<PortfolioEntity> portfolios = portfolioRepository.findAll().stream()
                    .filter(p -> p.getDeletedAt() == null)
                    .toList();

            int successCount = 0;
            for (PortfolioEntity portfolio : portfolios) {
                try {
                    createSnapshot(portfolio.getId(), snapshotTime);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to create snapshot for portfolio {}: {}",
                            portfolio.getId(), e.getMessage());
                }
            }

            log.info("Completed snapshot job: {}/{} portfolios", successCount, portfolios.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Snapshot job interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void createSnapshot(UUID portfolioId, Instant snapshotTime) {
        PortfolioSummary summary = holdingsAggregationService.getPortfolioSummary(portfolioId);
        if (summary == null) {
            log.warn("Cannot create snapshot for portfolio {} - summary not available", portfolioId);
            return;
        }

        List<HoldingSnapshot> holdingSnapshots = summary.getHoldings().stream()
                .map(h -> HoldingSnapshot.builder()
                        .symbol(h.getSymbol())
                        .name(h.getName())
                        .quantity(h.getTotalQuantity())
                        .priceUsd(h.getPriceUsd())
                        .valueUsd(h.getValueUsd())
                        .build())
                .toList();

        PortfolioSnapshotEntity snapshot = PortfolioSnapshotEntity.builder()
                .portfolioId(portfolioId)
                .totalValueUsd(summary.getTotalValueUsd())
                .snapshotTime(snapshotTime)
                .holdings(holdingSnapshots)
                .chainBreakdown(summary.getChainBreakdown())
                .build();

        snapshotRepository.save(snapshot);
        log.debug("Created snapshot for portfolio {} at {}: ${}",
                portfolioId, snapshotTime, summary.getTotalValueUsd());
    }

    public List<PortfolioSnapshotEntity> getHistory(UUID portfolioId, String period) {
        Instant since = calculateSinceTime(period);
        return snapshotRepository.findByPortfolioIdAndSnapshotTimeAfter(portfolioId, since);
    }

    public PortfolioSnapshotEntity getLatestSnapshot(UUID portfolioId) {
        return snapshotRepository.findLatestByPortfolioId(portfolioId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public PortfolioSnapshotEntity get24hAgoSnapshot(UUID portfolioId) {
        Instant time24hAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        return snapshotRepository.findClosestBefore(portfolioId, time24hAgo, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Instant calculateSinceTime(String period) {
        return switch (period.toLowerCase()) {
            case "24h" -> Instant.now().minus(24, ChronoUnit.HOURS);
            case "7d" -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "30d" -> Instant.now().minus(30, ChronoUnit.DAYS);
            case "90d" -> Instant.now().minus(90, ChronoUnit.DAYS);
            case "1y" -> Instant.now().minus(365, ChronoUnit.DAYS);
            case "all" -> Instant.EPOCH;
            default -> Instant.now().minus(7, ChronoUnit.DAYS);
        };
    }
}
