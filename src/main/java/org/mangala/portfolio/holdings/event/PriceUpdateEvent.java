package org.mangala.portfolio.holdings.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateEvent {
    private String symbol;
    private String name;
    private BigDecimal priceUsd;
    private BigDecimal change24h;
    private Instant timestamp;
}
