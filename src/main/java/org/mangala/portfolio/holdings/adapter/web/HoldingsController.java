package org.mangala.portfolio.holdings.adapter.web;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.holdings.usecase.GetPortfolioSummaryUseCase;
import org.mangala.security.preauthenticated.PreAuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class HoldingsController {

    private final GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;

    @GetMapping("/{id}/summary")
    public ResponseEntity<GetPortfolioSummaryUseCase.PortfolioSummaryResponse> getSummary(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @RequestParam(required = false) String chainType) {
        UUID userId = UUID.fromString(principal.getUserId());

        var command = new GetPortfolioSummaryUseCase.GetSummaryCommand(id, userId, chainType);
        var response = getPortfolioSummaryUseCase.execute(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/holdings")
    public ResponseEntity<GetPortfolioSummaryUseCase.PortfolioSummaryResponse> getHoldings(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @RequestParam(required = false) String chainType) {
        // Holdings is essentially the same as summary for now
        return getSummary(principal, id, chainType);
    }
}
