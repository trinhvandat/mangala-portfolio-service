package org.mangala.portfolio.portfolio.usecase;

import java.util.List;
import java.util.UUID;

public interface ListPortfoliosUseCase {
    List<PortfolioSummary> execute(UUID userId);

    record PortfolioSummary(UUID id, String name, String description, boolean isDefault, int walletCount) {}
}
