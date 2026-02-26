package org.mangala.portfolio.holdings.service;

import org.mangala.portfolio.holdings.domain.PriceData;
import org.mangala.portfolio.holdings.event.PriceUpdateEvent;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceCacheService {
    void updatePrice(PriceUpdateEvent event);

    Optional<PriceData> getPrice(String symbol);

    BigDecimal getPriceUsd(String symbol);
}
