package org.mangala.portfolio.portfolio.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.domain.PortfolioWalletEntity;
import org.mangala.portfolio.portfolio.usecase.GetPortfolioUseCase;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPortfolioUseCaseImpl implements GetPortfolioUseCase {

    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional(readOnly = true)
    public GetPortfolioResponse execute(GetPortfolioCommand command) {
        PortfolioEntity portfolio = portfolioRepository.findByIdWithWallets(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        List<UUID> walletIds = portfolio.getWallets().stream()
                .map(PortfolioWalletEntity::getWalletId)
                .collect(Collectors.toList());

        return new GetPortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getIsDefault(),
                walletIds
        );
    }
}
