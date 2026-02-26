package org.mangala.portfolio.holdings.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mangala.portfolio.holdings.service.PriceCacheService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateConsumer {

    private final PriceCacheService priceCacheService;

    @KafkaListener(topics = "price.updates", groupId = "portfolio-service")
    public void handlePriceUpdate(PriceUpdateEvent event) {
        log.debug("Received price update for {}: ${}", event.getSymbol(), event.getPriceUsd());
        try {
            priceCacheService.updatePrice(event);
        } catch (Exception e) {
            log.error("Failed to process price update for {}: {}",
                    event.getSymbol(), e.getMessage(), e);
        }
    }
}
