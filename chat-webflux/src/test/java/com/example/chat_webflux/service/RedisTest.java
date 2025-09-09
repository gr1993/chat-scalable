package com.example.chat_webflux.service;

import com.example.chat_webflux.dto.WsJsonMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
public class RedisTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Autowired
    private RedisMessageSubscriber redisMessageSubscriber;

    @Test
    void reactiveRedisString() {
        // given
        String value = "home";

        // when
        reactiveRedisTemplate.opsForValue().set("address", value).block();

        // then
        String addressValue = reactiveRedisTemplate.opsForValue().get("address").block();
        Assertions.assertEquals(value, addressValue);
    }

    @Test
    void reactiveRedisPubSub() throws Exception {
        // given
        WsJsonMessage expectedMessage = new WsJsonMessage("send", "chat", "안녕하세요~", "test");
        Flux<WsJsonMessage> messageFlux = redisMessageSubscriber.getInboundMessageFlux();

        // when & then
        StepVerifier.create(messageFlux)
                .then(() -> redisMessagePublisher.publish(expectedMessage).subscribe())
                .expectNextMatches(receivedMessage -> {
                    // 수신된 메시지의 내용 검증
                    return receivedMessage.getType().equals(expectedMessage.getType()) &&
                            receivedMessage.getChannel().equals(expectedMessage.getChannel()) &&
                            receivedMessage.getMessage().equals(expectedMessage.getMessage()) &&
                            receivedMessage.getRoomId().equals(expectedMessage.getRoomId());
                })
                .thenCancel() // 테스트 완료 후 구독 취소
                .verify();
    }
}