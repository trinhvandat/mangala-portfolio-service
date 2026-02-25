package org.mangala.portfolio.holdings.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.holdings.domain.PortfolioSummary;
import org.mangala.portfolio.holdings.service.HoldingsAggregationService;
import org.mangala.portfolio.holdings.usecase.GetPortfolioSummaryUseCase;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.mangala.portfolio.snapshot.domain.PortfolioSnapshotEntity;
import org.mangala.portfolio.snapshot.service.PortfolioSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPortfolioSummaryUseCaseImpl implements GetPortfolioSummaryUseCase {

    private final PortfolioRepository portfolioRepository;
    private final HoldingsAggregationService holdingsAggregationService;
    private final PortfolioSnapshotService snapshotService;

    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryResponse execute(GetSummaryCommand command) {
        var portfolio = portfolioRepository.findByIdAndNotDeleted(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        PortfolioSummary summary = holdingsAggregationService.getPortfolioSummary(command.portfolioId());
        if (summary == null) {
            throw new PortfolioNotFoundException();
        }

        // Calculate 24h change from snapshots
        BigDecimal change24hUsd = BigDecimal.ZERO;
        BigDecimal change24hPercent = BigDecimal.ZERO;

        PortfolioSnapshotEntity snapshot24hAgo = snapshotService.get24hAgoSnapshot(command.portfolioId());
        if (snapshot24hAgo != null && snapshot24hAgo.getTotalValueUsd().compareTo(BigDecimal.ZERO) != 0) {
            change24hUsd = summary.getTotalValueUsd().subtract(snapshot24hAgo.getTotalValueUsd());
            change24hPercent = change24hUsd
                    .divide(snapshot24hAgo.getTotalValueUsd(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<HoldingDTO> holdingDTOs = summary.getHoldings().stream()
                .filter(h -> command.chainType() == null || h.getChains().contains(command.chainType()))
                .map(h -> new HoldingDTO(
                        h.getSymbol(),
                        h.getName(),
                        h.getTotalQuantity(),
                        h.getPriceUsd(),
                        h.getValueUsd(),
                        h.getChange24hPercent(),
                        h.getAllocation(),
                        h.getChains()
                ))
                .collect(Collectors.toList());

        return new PortfolioSummaryResponse(
                command.portfolioId(),
                summary.getTotalValueUsd(),
                change24hUsd,
                change24hPercent,
                holdingDTOs,
                summary.getChainBreakdown(),
                summary.getLastUpdated()
        );
    }
}
