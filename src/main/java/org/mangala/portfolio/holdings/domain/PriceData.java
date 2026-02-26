package org.mangala.portfolio.holdings.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceData implements Serializable {
    private String symbol;
    private BigDecimal priceUsd;
    private BigDecimal change24h;
    private Instant updatedAt;
}
