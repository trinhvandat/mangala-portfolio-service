package org.mangala.portfolio.portfolio.usecase;

import java.util.UUID;

public interface UpdatePortfolioUseCase {
    UpdatePortfolioResponse execute(UpdatePortfolioCommand command);

    record UpdatePortfolioCommand(UUID portfolioId, UUID userId, String name, String description) {}
    record UpdatePortfolioResponse(UUID id, String name, String description) {}
}
