package org.mangala.portfolio.holdings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mangala.portfolio.holdings.domain.PriceData;
import org.mangala.portfolio.holdings.event.PriceUpdateEvent;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceCacheServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<PriceData> bucket;

    private PriceCacheServiceImpl priceCacheService;

    @BeforeEach
    void setUp() {
        priceCacheService = new PriceCacheServiceImpl(redissonClient);
    }

    @Test
    void updatePrice_shouldCachePriceData() {
        // Given
        PriceUpdateEvent event = PriceUpdateEvent.builder()
                .symbol("ETH")
                .priceUsd(new BigDecimal("2500.00"))
                .change24h(new BigDecimal("2.5"))
                .timestamp(Instant.now())
                .build();

        when(redissonClient.<PriceData>getBucket("price:ETH")).thenReturn(bucket);

        // When
        priceCacheService.updatePrice(event);

        // Then
        verify(bucket).set(any(PriceData.class), eq(Duration.ofSeconds(60)));
    }

    @Test
    void getPrice_shouldReturnCachedPrice_whenExists() {
        // Given
        PriceData priceData = PriceData.builder()
                .symbol("ETH")
                .priceUsd(new BigDecimal("2500.00"))
                .change24h(new BigDecimal("2.5"))
                .updatedAt(Instant.now())
                .build();

        when(redissonClient.<PriceData>getBucket("price:ETH")).thenReturn(bucket);
        when(bucket.get()).thenReturn(priceData);

        // When
        Optional<PriceData> result = priceCacheService.getPrice("ETH");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPriceUsd()).isEqualTo(new BigDecimal("2500.00"));
    }

    @Test
    void getPrice_shouldReturnEmpty_whenNotCached() {
        // Given
        when(redissonClient.<PriceData>getBucket("price:BTC")).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        // When
        Optional<PriceData> result = priceCacheService.getPrice("BTC");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPriceUsd_shouldReturnPrice_whenCached() {
        // Given
        PriceData priceData = PriceData.builder()
                .symbol("ETH")
                .priceUsd(new BigDecimal("2500.00"))
                .build();

        when(redissonClient.<PriceData>getBucket("price:ETH")).thenReturn(bucket);
        when(bucket.get()).thenReturn(priceData);

        // When
        BigDecimal result = priceCacheService.getPriceUsd("ETH");

        // Then
        assertThat(result).isEqualTo(new BigDecimal("2500.00"));
    }

    @Test
    void getPriceUsd_shouldReturnNull_whenNotCached() {
        // Given
        when(redissonClient.<PriceData>getBucket("price:BTC")).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        // When
        BigDecimal result = priceCacheService.getPriceUsd("BTC");

        // Then
        assertThat(result).isNull();
    }
}
