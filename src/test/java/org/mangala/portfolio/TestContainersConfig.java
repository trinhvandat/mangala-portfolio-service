package org.mangala.portfolio;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test configuration that disables external dependencies and provides mocks.
 * This allows tests to run without requiring Docker or external services.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
public class TestContainersConfig {

    @MockBean
    private RedissonClient redissonClient;
}
