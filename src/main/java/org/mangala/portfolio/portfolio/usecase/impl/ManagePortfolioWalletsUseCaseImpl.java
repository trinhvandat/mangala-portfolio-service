package org.mangala.portfolio.portfolio.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioWalletRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.domain.PortfolioWalletEntity;
import org.mangala.portfolio.portfolio.usecase.ManagePortfolioWalletsUseCase;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.mangala.portfolio.shared.exception.WalletLimitExceededException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagePortfolioWalletsUseCaseImpl implements ManagePortfolioWalletsUseCase {

    private static final int MAX_WALLETS_PER_PORTFOLIO = 10;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioWalletRepository portfolioWalletRepository;

    @Override
    @Transactional
    public ManageWalletsResponse addWallets(AddWalletsCommand command) {
        PortfolioEntity portfolio = portfolioRepository.findByIdWithWallets(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        Set<UUID> existingWalletIds = portfolio.getWallets().stream()
                .map(PortfolioWalletEntity::getWalletId)
                .collect(Collectors.toSet());

        List<UUID> newWalletIds = command.walletIds().stream()
                .filter(id -> !existingWalletIds.contains(id))
                .toList();

        int totalAfterAdd = portfolio.getWallets().size() + newWalletIds.size();
        if (totalAfterAdd > MAX_WALLETS_PER_PORTFOLIO) {
            throw new WalletLimitExceededException();
        }

        for (UUID walletId : newWalletIds) {
            PortfolioWalletEntity pw = PortfolioWalletEntity.builder()
                    .portfolioId(portfolio.getId())
                    .walletId(walletId)
                    .build();
            portfolioWalletRepository.save(pw);
        }

        List<UUID> allWalletIds = portfolioWalletRepository.findWalletIdsByPortfolioId(portfolio.getId());

        return new ManageWalletsResponse(portfolio.getId(), allWalletIds, allWalletIds.size());
    }

    @Override
    @Transactional
    public void removeWallet(RemoveWalletCommand command) {
        PortfolioEntity portfolio = portfolioRepository.findByIdAndNotDeleted(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        portfolioWalletRepository.deleteByPortfolioIdAndWalletId(command.portfolioId(), command.walletId());
    }
}
