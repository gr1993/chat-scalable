package com.example.chat_webflux.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

/**
 * Embedded Redis Server
 * 테스트 실행 시 자체 Redis를 띄움
 */
@Import(LocalRedisConfig.class)
@SpringBootTest
public class RedisIntegrationTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    void reactiveRedisString() {
        // given
        String address = "myHome";
        reactiveRedisTemplate.opsForValue().set("address", address).block();

        // when
        String savedAddress = reactiveRedisTemplate.opsForValue().get("address").block();

        // then
        Assertions.assertEquals(address, savedAddress);
    }
}
