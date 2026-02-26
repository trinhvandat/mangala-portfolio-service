package org.mangala.portfolio.portfolio.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_wallets")
@IdClass(PortfolioWalletId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioWalletEntity {
    @Id
    @Column(name = "portfolio_id")
    private UUID portfolioId;

    @Id
    @Column(name = "wallet_id")
    private UUID walletId;

    @Column(name = "added_at", nullable = false)
    @Builder.Default
    private Instant addedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", insertable = false, updatable = false)
    private PortfolioEntity portfolio;
}
