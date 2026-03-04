package org.mangala.portfolio.portfolio.adapter.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.web.dto.*;
import org.mangala.portfolio.portfolio.usecase.*;
import org.mangala.security.preauthenticated.PreAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final CreatePortfolioUseCase createPortfolioUseCase;
    private final GetPortfolioUseCase getPortfolioUseCase;
    private final ListPortfoliosUseCase listPortfoliosUseCase;
    private final UpdatePortfolioUseCase updatePortfolioUseCase;
    private final DeletePortfolioUseCase deletePortfolioUseCase;
    private final ManagePortfolioWalletsUseCase managePortfolioWalletsUseCase;

    @PostMapping
    public ResponseEntity<PortfolioResponseDTO> createPortfolio(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @Valid @RequestBody CreatePortfolioRequestDTO request) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new CreatePortfolioUseCase.CreatePortfolioCommand(
                userId, request.getName(), request.getDescription(), request.getWalletIds());

        var response = createPortfolioUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioResponseDTO.builder()
                .id(response.id())
                .name(response.name())
                .description(response.description())
                .walletIds(response.walletIds())
                .build());
    }

    @GetMapping
    public ResponseEntity<List<PortfolioSummaryDTO>> listPortfolios(@AuthenticationPrincipal PreAuthenticatedPrincipal principal) {
        UUID userId = UUID.fromString(principal.getUserId());

        var portfolios = listPortfoliosUseCase.execute(userId).stream()
                .map(p -> PortfolioSummaryDTO.builder()
                        .id(p.id())
                        .name(p.name())
                        .description(p.description())
                        .isDefault(p.isDefault())
                        .walletCount(p.walletCount())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponseDTO> getPortfolio(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new GetPortfolioUseCase.GetPortfolioCommand(id, userId);
        var response = getPortfolioUseCase.execute(command);

        return ResponseEntity.ok(PortfolioResponseDTO.builder()
                .id(response.id())
                .name(response.name())
                .description(response.description())
                .isDefault(response.isDefault())
                .walletIds(response.walletIds())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponseDTO> updatePortfolio(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePortfolioRequestDTO request) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new UpdatePortfolioUseCase.UpdatePortfolioCommand(
                id, userId, request.getName(), request.getDescription());

        var response = updatePortfolioUseCase.execute(command);

        return ResponseEntity.ok(PortfolioResponseDTO.builder()
                .id(response.id())
                .name(response.name())
                .description(response.description())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new DeletePortfolioUseCase.DeletePortfolioCommand(id, userId);
        deletePortfolioUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/wallets")
    public ResponseEntity<PortfolioResponseDTO> addWallets(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody AddWalletsRequestDTO request) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new ManagePortfolioWalletsUseCase.AddWalletsCommand(id, userId, request.getWalletIds());
        var response = managePortfolioWalletsUseCase.addWallets(command);

        return ResponseEntity.ok(PortfolioResponseDTO.builder()
                .id(response.portfolioId())
                .walletIds(response.walletIds())
                .build());
    }

    @DeleteMapping("/{id}/wallets/{walletId}")
    public ResponseEntity<Void> removeWallet(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @PathVariable UUID walletId) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new ManagePortfolioWalletsUseCase.RemoveWalletCommand(id, userId, walletId);
        managePortfolioWalletsUseCase.removeWallet(command);

        return ResponseEntity.noContent().build();
    }
}
