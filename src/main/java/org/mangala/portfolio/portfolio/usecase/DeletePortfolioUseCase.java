package org.mangala.portfolio.portfolio.usecase;

import java.util.UUID;

public interface DeletePortfolioUseCase {
    void execute(DeletePortfolioCommand command);

    record DeletePortfolioCommand(UUID portfolioId, UUID userId) {}
}
