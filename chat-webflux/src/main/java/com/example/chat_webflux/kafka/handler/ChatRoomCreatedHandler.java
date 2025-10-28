package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomCreatedHandler implements KafkaEventHandler<ChatRoom> {

    private final ReactiveRedisTemplate<String, Object> redisObjectTemplate;
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaSender;

    /**
     * Redis에 캐싱 후 알림 토픽으로 전달
     */
    @Override
    public Mono<Void> handle(ChatRoom event) {

        Mono<Void> redisSaveMono = redisObjectTemplate.opsForValue()
                .set("room:" + event.getId(), event)
                .then();

        Mono<Void> notificationMono = kafkaSender.sendTransactionally(
                Flux.just(
                        SenderRecord.create(
                                KafkaTopics.CHAT_ROOM_NOTIFICATION,
                                null,
                                null,
                                event.getId().toString(),
                                event,
                                null
                        )
                )
        ).then();

        return redisSaveMono
                .doOnSuccess(v -> log.info("Saved chat room {} to Redis", event.getId()))
                .then(notificationMono)
                .doOnSuccess(v -> log.info("Sent chat room notification for {}", event.getId()));
    }

    @Override
    public String getTopic() {
        return KafkaTopics.CHAT_ROOM_CREATED;
    }
}
