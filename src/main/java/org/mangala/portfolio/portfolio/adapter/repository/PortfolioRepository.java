package org.mangala.portfolio.portfolio.adapter.repository;

import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioEntity, UUID> {

    @Query("SELECT p FROM PortfolioEntity p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    List<PortfolioEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM PortfolioEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<PortfolioEntity> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT p FROM PortfolioEntity p WHERE p.id = :id AND p.userId = :userId AND p.deletedAt IS NULL")
    Optional<PortfolioEntity> findByIdAndUserIdAndNotDeleted(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT COUNT(p) FROM PortfolioEntity p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM PortfolioEntity p WHERE p.userId = :userId AND p.isDefault = true AND p.deletedAt IS NULL")
    Optional<PortfolioEntity> findDefaultByUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM PortfolioEntity p LEFT JOIN FETCH p.wallets WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<PortfolioEntity> findByIdWithWallets(@Param("id") UUID id);
}
