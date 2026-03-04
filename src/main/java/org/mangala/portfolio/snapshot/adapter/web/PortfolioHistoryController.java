package org.mangala.portfolio.snapshot.adapter.web;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.snapshot.usecase.GetPortfolioHistoryUseCase;
import org.mangala.security.preauthenticated.PreAuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioHistoryController {

    private final GetPortfolioHistoryUseCase getPortfolioHistoryUseCase;

    @GetMapping("/{id}/history")
    public ResponseEntity<GetPortfolioHistoryUseCase.GetHistoryResponse> getHistory(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "7d") String period) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new GetPortfolioHistoryUseCase.GetHistoryCommand(id, userId, period);
        var response = getPortfolioHistoryUseCase.execute(command);

        return ResponseEntity.ok(response);
    }
}
