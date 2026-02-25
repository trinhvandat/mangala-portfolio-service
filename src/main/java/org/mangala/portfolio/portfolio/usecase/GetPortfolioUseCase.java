package org.mangala.portfolio.portfolio.usecase;

import java.util.List;
import java.util.UUID;

public interface GetPortfolioUseCase {
    GetPortfolioResponse execute(GetPortfolioCommand command);

    record GetPortfolioCommand(UUID portfolioId, UUID userId) {}
    record GetPortfolioResponse(UUID id, String name, String description, boolean isDefault, List<UUID> walletIds) {}
}
