package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatRoomHandler implements KafkaEventHandler<ChatRoom> {

    private final ReactiveRedisTemplate<String, Object> redisObjectTemplate;

    @Override
    public Mono<Void> handle(ChatRoom event) {
        return redisObjectTemplate.opsForValue()
                .set("room:" + event.getId(), event)
                .then();
    }

    @Override
    public Class<ChatRoom> getEventType() {
        return ChatRoom.class;
    }
}
