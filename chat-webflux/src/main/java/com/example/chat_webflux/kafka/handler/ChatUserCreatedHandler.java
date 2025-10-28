package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatUserCreatedHandler implements KafkaEventHandler<ChatUser> {

    private final ReactiveRedisTemplate<String, Object> redisObjectTemplate;

    @Override
    public Mono<Void> handle(ChatUser event) {
        return redisObjectTemplate.opsForValue()
                .set("user:" + event.getId(), event)
                .then();
    }

    @Override
    public String getTopic() {
        return KafkaTopics.CHAT_USER_CREATED;
    }
}
