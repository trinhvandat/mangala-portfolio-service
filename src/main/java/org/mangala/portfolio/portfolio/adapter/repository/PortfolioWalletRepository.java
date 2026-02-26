package org.mangala.portfolio.portfolio.adapter.repository;

import org.mangala.portfolio.portfolio.domain.PortfolioWalletEntity;
import org.mangala.portfolio.portfolio.domain.PortfolioWalletId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioWalletRepository extends JpaRepository<PortfolioWalletEntity, PortfolioWalletId> {

    @Query("SELECT pw.walletId FROM PortfolioWalletEntity pw WHERE pw.portfolioId = :portfolioId")
    List<UUID> findWalletIdsByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT COUNT(pw) FROM PortfolioWalletEntity pw WHERE pw.portfolioId = :portfolioId")
    long countByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT pw FROM PortfolioWalletEntity pw WHERE pw.walletId = :walletId")
    List<PortfolioWalletEntity> findByWalletId(@Param("walletId") UUID walletId);

    void deleteByPortfolioIdAndWalletId(UUID portfolioId, UUID walletId);
}
