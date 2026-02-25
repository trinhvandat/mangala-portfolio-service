package org.mangala.portfolio.holdings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mangala.portfolio.holdings.domain.PriceData;
import org.mangala.portfolio.holdings.event.PriceUpdateEvent;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceCacheServiceImpl implements PriceCacheService {

    private static final String PRICE_KEY_PREFIX = "price:";
    private static final Duration PRICE_TTL = Duration.ofSeconds(60);

    private final RedissonClient redissonClient;

    @Override
    public void updatePrice(PriceUpdateEvent event) {
        String key = PRICE_KEY_PREFIX + event.getSymbol().toUpperCase();
        RBucket<PriceData> bucket = redissonClient.getBucket(key);

        PriceData priceData = PriceData.builder()
                .symbol(event.getSymbol())
                .priceUsd(event.getPriceUsd())
                .change24h(event.getChange24h())
                .updatedAt(event.getTimestamp())
                .build();

        bucket.set(priceData, PRICE_TTL);
        log.debug("Cached price for {}: ${}", event.getSymbol(), event.getPriceUsd());
    }

    @Override
    public Optional<PriceData> getPrice(String symbol) {
        String key = PRICE_KEY_PREFIX + symbol.toUpperCase();
        RBucket<PriceData> bucket = redissonClient.getBucket(key);
        PriceData data = bucket.get();
        return Optional.ofNullable(data);
    }

    @Override
    public BigDecimal getPriceUsd(String symbol) {
        return getPrice(symbol)
                .map(PriceData::getPriceUsd)
                .orElse(null);
    }
}
