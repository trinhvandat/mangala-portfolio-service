package org.mangala.portfolio.holdings.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedHolding {
    private String symbol;
    private String name;
    private BigDecimal totalQuantity;
    private BigDecimal priceUsd;
    private BigDecimal valueUsd;
    private BigDecimal change24hPercent;
    private BigDecimal allocation;
    private List<String> chains;
}
