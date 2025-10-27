package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.kafka.KafkaEvent;
import reactor.core.publisher.Mono;

public interface KafkaEventHandler<T extends KafkaEvent> {
    Mono<Void> handle(T event);
    Class<T> getEventType();
}
