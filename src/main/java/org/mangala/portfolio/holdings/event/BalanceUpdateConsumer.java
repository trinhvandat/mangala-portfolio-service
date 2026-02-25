package org.mangala.portfolio.holdings.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mangala.portfolio.holdings.service.HoldingsAggregationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceUpdateConsumer {

    private final HoldingsAggregationService holdingsAggregationService;

    @KafkaListener(topics = "balance.updates", groupId = "portfolio-service")
    public void handleBalanceUpdate(BalanceUpdateEvent event) {
        log.debug("Received balance update for wallet {}: {} tokens",
                event.getWalletId(), event.getBalances() != null ? event.getBalances().size() : 0);
        try {
            holdingsAggregationService.processBalanceUpdate(event);
        } catch (Exception e) {
            log.error("Failed to process balance update for wallet {}: {}",
                    event.getWalletId(), e.getMessage(), e);
        }
    }
}
