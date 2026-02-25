package org.mangala.portfolio.snapshot.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.mangala.portfolio.portfolio.adapter.repository.PortfolioRepository;
import org.mangala.portfolio.shared.exception.PortfolioAccessDeniedException;
import org.mangala.portfolio.shared.exception.PortfolioNotFoundException;
import org.mangala.portfolio.snapshot.domain.PortfolioSnapshotEntity;
import org.mangala.portfolio.snapshot.service.PortfolioSnapshotService;
import org.mangala.portfolio.snapshot.usecase.GetPortfolioHistoryUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPortfolioHistoryUseCaseImpl implements GetPortfolioHistoryUseCase {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioSnapshotService snapshotService;

    @Override
    @Transactional(readOnly = true)
    public GetHistoryResponse execute(GetHistoryCommand command) {
        var portfolio = portfolioRepository.findByIdAndNotDeleted(command.portfolioId())
                .orElseThrow(PortfolioNotFoundException::new);

        if (!portfolio.getUserId().equals(command.userId())) {
            throw new PortfolioAccessDeniedException();
        }

        List<PortfolioSnapshotEntity> snapshots = snapshotService.getHistory(command.portfolioId(), command.period());

        if (snapshots.isEmpty()) {
            return new GetHistoryResponse(
                    command.period(),
                    List.of(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        List<DataPoint> dataPoints = snapshots.stream()
                .sorted(Comparator.comparing(PortfolioSnapshotEntity::getSnapshotTime))
                .map(s -> new DataPoint(s.getSnapshotTime(), s.getTotalValueUsd()))
                .toList();

        BigDecimal startValue = dataPoints.getFirst().valueUsd();
        BigDecimal endValue = dataPoints.getLast().valueUsd();
        BigDecimal changeUsd = endValue.subtract(startValue);
        BigDecimal changePercent = startValue.compareTo(BigDecimal.ZERO) != 0
                ? changeUsd.divide(startValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal highValue = dataPoints.stream()
                .map(DataPoint::valueUsd)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal lowValue = dataPoints.stream()
                .map(DataPoint::valueUsd)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return new GetHistoryResponse(
                command.period(),
                dataPoints,
                startValue,
                endValue,
                changeUsd,
                changePercent,
                highValue,
                lowValue
        );
    }
}
