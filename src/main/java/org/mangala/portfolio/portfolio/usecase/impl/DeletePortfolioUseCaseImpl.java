package org.mangala.portfolio.portfolio.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.usecase.DeletePortfolioUseCase;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePortfolioUseCaseImpl implements DeletePortfolioUseCase {

    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public void execute(DeletePortfolioCommand command) {
        PortfolioEntity portfolio = portfolioRepository.findByIdAndNotDeleted(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        // Soft delete
        portfolio.softDelete();
        portfolioRepository.save(portfolio);
    }
}
