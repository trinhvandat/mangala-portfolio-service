package org.mangala.portfolio.holdings.service;

import org.mangala.portfolio.holdings.domain.PortfolioSummary;
import org.mangala.portfolio.holdings.event.BalanceUpdateEvent;

import java.util.UUID;

public interface HoldingsAggregationService {
    void processBalanceUpdate(BalanceUpdateEvent event);

    PortfolioSummary getPortfolioSummary(UUID portfolioId);

    void invalidateCache(UUID portfolioId);
}
