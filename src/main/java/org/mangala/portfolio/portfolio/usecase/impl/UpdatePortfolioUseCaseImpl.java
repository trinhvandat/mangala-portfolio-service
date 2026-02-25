package org.mangala.portfolio.portfolio.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.portfolio.domain.PortfolioEntity;
import org.mangala.portfolio.portfolio.usecase.UpdatePortfolioUseCase;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePortfolioUseCaseImpl implements UpdatePortfolioUseCase {

    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public UpdatePortfolioResponse execute(UpdatePortfolioCommand command) {
        PortfolioEntity portfolio = portfolioRepository.findByIdAndNotDeleted(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        if (command.name() != null) {
            portfolio.setName(command.name());
        }
        if (command.description() != null) {
            portfolio.setDescription(command.description());
        }

        PortfolioEntity saved = portfolioRepository.save(portfolio);
        return new UpdatePortfolioResponse(saved.getId(), saved.getName(), saved.getDescription());
    }
}
