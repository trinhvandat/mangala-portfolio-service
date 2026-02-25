package org.mangala.portfolio.portfolio.usecase;

import java.util.List;
import java.util.UUID;

public interface CreatePortfolioUseCase {
    CreatePortfolioResponse execute(CreatePortfolioCommand command);

    record CreatePortfolioCommand(UUID userId, String name, String description, List<UUID> walletIds) {}
    record CreatePortfolioResponse(UUID id, String name, String description, List<UUID> walletIds) {}
}
