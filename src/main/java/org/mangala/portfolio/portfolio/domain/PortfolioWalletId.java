package org.mangala.portfolio.portfolio.domain;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioWalletId implements Serializable {
    private UUID portfolioId;
    private UUID walletId;
}
