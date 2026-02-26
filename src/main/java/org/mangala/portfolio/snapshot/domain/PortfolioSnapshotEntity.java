package org.mangala.portfolio.snapshot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "portfolio_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "total_value_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalValueUsd;

    @Column(name = "snapshot_time", nullable = false)
    private Instant snapshotTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "holdings", nullable = false, columnDefinition = "jsonb")
    private List<HoldingSnapshot> holdings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chain_breakdown", columnDefinition = "jsonb")
    private Map<String, BigDecimal> chainBreakdown;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
