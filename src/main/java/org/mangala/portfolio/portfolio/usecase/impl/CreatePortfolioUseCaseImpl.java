package org.mangala.portfolio.portfolio.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.domain.PortfolioWalletEntity;
import org.mangala.portfolio.portfolio.usecase.CreatePortfolioUseCase;
import org.mangala.portfolio.shared.exception.WalletLimitExceededException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreatePortfolioUseCaseImpl implements CreatePortfolioUseCase {

    private static final int MAX_WALLETS_PER_PORTFOLIO = 10;
    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public CreatePortfolioResponse execute(CreatePortfolioCommand command) {
        if (command.walletIds() != null && command.walletIds().size() > MAX_WALLETS_PER_PORTFOLIO) {
            throw new WalletLimitExceededException();
        }

        PortfolioEntity portfolio = PortfolioEntity.builder()
                .userId(command.userId())
                .name(command.name())
                .description(command.description())
                .build();

        if (command.walletIds() != null) {
            for (UUID walletId : command.walletIds()) {
                PortfolioWalletEntity pw = PortfolioWalletEntity.builder()
                        .portfolioId(portfolio.getId())
                        .walletId(walletId)
                        .build();
                pw.setPortfolio(portfolio);
                portfolio.getWallets().add(pw);
            }
        }

        PortfolioEntity saved = portfolioRepository.save(portfolio);

        List<UUID> savedWalletIds = saved.getWallets().stream()
                .map(PortfolioWalletEntity::getWalletId)
                .collect(Collectors.toList());

        return new CreatePortfolioResponse(saved.getId(), saved.getName(), saved.getDescription(), savedWalletIds);
    }
}
