package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.entity.OutboxEventStatus;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaSender;
    private final RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        startPollingOutbox();
    }

    public void startPollingOutbox() {
        Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> checkOutboxAndPublishWithLock())
                .subscribe();
    }

    public Mono<OutboxEvent> saveOutboxEvent(String eventType, Map<String, Object> payloadMap) {
        OutboxEvent outboxEvent = new OutboxEvent(eventType);

        try {
            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            outboxEvent.setPayload(payloadJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        return outboxEventRepository.save(outboxEvent);
    }

    public Mono<Void> checkOutboxAndPublish() {
        return outboxEventRepository.findByStatus(OutboxEventStatus.PENDING.name())
                .switchIfEmpty(Mono.empty())
                .flatMap(event ->
                    Mono.fromCallable(() -> {
                        // JSON → Map<String, Object>
                        return objectMapper.readValue(event.getPayload(), new TypeReference<Map<String, Object>>() {});
                    })
                    .flatMap(payloadMap -> {
                        if (KafkaTopics.CHAT_USER_CREATED.equals(event.getEventType())) {
                            ChatUser chatUser = new ChatUser((String)payloadMap.get("id"));
                            return kafkaSender.send(KafkaTopics.CHAT_USER_CREATED, chatUser);
                        }
                        return Mono.empty();
                    })
                )
                .then();
    }

    /**
     * 다중 서버가 실행되는 환경으로 분산 락 적용
     */
    private Mono<Void> checkOutboxAndPublishWithLock() {
        RLock lock = redissonClient.getLock("outbox-polling-lock");

        return Mono.usingWhen(
                Mono.fromCallable(() -> {
                    boolean locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
                    return locked;
                }),
                locked -> {
                    if (locked) {
                        // 락을 획득한 인스턴스만 실제 로직 실행
                        return checkOutboxAndPublish();
                    } else {
                        // 락을 못 잡은 인스턴스는 그냥 skip
                        return Mono.empty();
                    }
                },
                locked -> {
                    if (Boolean.TRUE.equals(locked) && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                    return Mono.empty();
                }
        );
    }
}
