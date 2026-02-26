package org.mangala.portfolio.holdings.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummary {
    private UUID portfolioId;
    private BigDecimal totalValueUsd;
    private BigDecimal change24hUsd;
    private BigDecimal change24hPercent;
    private List<AggregatedHolding> holdings;
    private Map<String, BigDecimal> chainBreakdown;
    private Instant lastUpdated;
}
