package org.mangala.portfolio.snapshot.domain;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingSnapshot {
    private String symbol;
    private String name;
    private BigDecimal quantity;
    private BigDecimal priceUsd;
    private BigDecimal valueUsd;
}
