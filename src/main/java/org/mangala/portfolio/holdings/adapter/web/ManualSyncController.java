package org.mangala.portfolio.holdings.adapter.web;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.holdings.service.HoldingsAggregationService;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.mangala.security.preauthenticated.PreAuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class ManualSyncController {

    private final PortfolioRepository portfolioRepository;
    private final HoldingsAggregationService holdingsAggregationService;

    @PostMapping("/{id}/sync")
    public ResponseEntity<Map<String, String>> triggerSync(
            @AuthenticationPrincipal PreAuthenticatedPrincipal principal,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(principal.getUserId());

        var portfolio = portfolioRepository.findByIdAndNotDeleted(id)
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(userId)) {
            throw new PortfolioAccessDeniedException();
        }

        // Invalidate cache to force recalculation
        holdingsAggregationService.invalidateCache(id);

        return ResponseEntity.accepted().body(Map.of(
                "status", "sync_triggered",
                "portfolioId", id.toString()
        ));
    }
}
