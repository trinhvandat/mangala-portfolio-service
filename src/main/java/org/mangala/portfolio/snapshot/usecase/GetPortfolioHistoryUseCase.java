package org.mangala.portfolio.snapshot.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GetPortfolioHistoryUseCase {
    GetHistoryResponse execute(GetHistoryCommand command);

    record GetHistoryCommand(UUID portfolioId, UUID userId, String period) {}
    record GetHistoryResponse(
            String period,
            List<DataPoint> dataPoints,
            BigDecimal startValue,
            BigDecimal endValue,
            BigDecimal changeUsd,
            BigDecimal changePercent,
            BigDecimal highValue,
            BigDecimal lowValue
    ) {}
    record DataPoint(Instant timestamp, BigDecimal valueUsd) {}
}
