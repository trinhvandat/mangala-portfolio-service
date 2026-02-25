package org.mangala.portfolio.portfolio.usecase;

import java.util.List;
import java.util.UUID;

public interface ManagePortfolioWalletsUseCase {
    ManageWalletsResponse addWallets(AddWalletsCommand command);
    void removeWallet(RemoveWalletCommand command);

    record AddWalletsCommand(UUID portfolioId, UUID userId, List<UUID> walletIds) {}
    record RemoveWalletCommand(UUID portfolioId, UUID userId, UUID walletId) {}
    record ManageWalletsResponse(UUID portfolioId, List<UUID> walletIds, int totalWallets) {}
}
