package com.example.chat_webflux.service;

import com.example.chat_webflux.dto.WsJsonMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final String TOPIC_NAME = "chat-channel";

    public Mono<Long> publish(WsJsonMessage message) {
        return reactiveRedisTemplate.convertAndSend(TOPIC_NAME, message);
    }
}