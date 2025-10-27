package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.kafka.KafkaEvent;

public interface KafkaEventHandler<T extends KafkaEvent> {
    void handle(T event);
    Class<T> getEventType();
}
