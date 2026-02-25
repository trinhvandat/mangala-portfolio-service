package org.mangala.portfolio.holdings.usecase;

import org.mangala.portfolio.holdings.domain.AggregatedHolding;
import org.mangala.portfolio.holdings.domain.PortfolioSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetPortfolioSummaryUseCase {
    PortfolioSummaryResponse execute(GetSummaryCommand command);

    record GetSummaryCommand(UUID portfolioId, UUID userId, String chainType) {}
    record PortfolioSummaryResponse(
            UUID portfolioId,
            BigDecimal totalValueUsd,
            BigDecimal change24hUsd,
            BigDecimal change24hPercent,
            List<HoldingDTO> holdings,
            Map<String, BigDecimal> chainBreakdown,
            Instant lastUpdated
    ) {}
    record HoldingDTO(
            String symbol,
            String name,
            BigDecimal quantity,
            BigDecimal priceUsd,
            BigDecimal valueUsd,
            BigDecimal change24hPercent,
            BigDecimal allocation,
            List<String> chains
    ) {}
}
