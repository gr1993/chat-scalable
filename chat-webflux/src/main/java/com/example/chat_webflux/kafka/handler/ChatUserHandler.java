package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.entity.ChatUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatUserHandler implements KafkaEventHandler<ChatUser> {

    private final ReactiveRedisTemplate<String, Object> redisObjectTemplate;

    @Override
    public Mono<Void> handle(ChatUser event) {
        return redisObjectTemplate.opsForValue()
                .set("user:" + event.getId(), event)
                .then();
    }

    @Override
    public Class<ChatUser> getEventType() {
        return ChatUser.class;
    }
}
