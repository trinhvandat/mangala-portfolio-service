package org.mangala.portfolio.snapshot.adapter.repository;

import org.mangala.portfolio.snapshot.domain.PortfolioSnapshotEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshotEntity, UUID> {

    @Query("SELECT s FROM PortfolioSnapshotEntity s WHERE s.portfolioId = :portfolioId AND s.snapshotTime >= :since ORDER BY s.snapshotTime DESC")
    List<PortfolioSnapshotEntity> findByPortfolioIdAndSnapshotTimeAfter(
            @Param("portfolioId") UUID portfolioId,
            @Param("since") Instant since);

    @Query("SELECT s FROM PortfolioSnapshotEntity s WHERE s.portfolioId = :portfolioId ORDER BY s.snapshotTime DESC")
    List<PortfolioSnapshotEntity> findLatestByPortfolioId(@Param("portfolioId") UUID portfolioId, Pageable pageable);

    @Query("SELECT s FROM PortfolioSnapshotEntity s WHERE s.portfolioId = :portfolioId AND s.snapshotTime <= :time ORDER BY s.snapshotTime DESC")
    List<PortfolioSnapshotEntity> findClosestBefore(@Param("portfolioId") UUID portfolioId, @Param("time") Instant time, Pageable pageable);

    @Query("SELECT s FROM PortfolioSnapshotEntity s WHERE s.portfolioId = :portfolioId AND s.snapshotTime BETWEEN :start AND :end ORDER BY s.snapshotTime ASC")
    List<PortfolioSnapshotEntity> findByPortfolioIdAndTimeRange(
            @Param("portfolioId") UUID portfolioId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
