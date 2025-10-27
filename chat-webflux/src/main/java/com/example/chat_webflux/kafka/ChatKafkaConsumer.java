package com.example.chat_webflux.kafka;

import com.example.chat_webflux.kafka.handler.KafkaEventHandler;
import com.example.chat_webflux.kafka.handler.KafkaEventHandlerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatKafkaConsumer {

    private final ReactiveKafkaConsumerTemplate<String, KafkaEvent> outboxConsumer;
    private final ReactiveKafkaConsumerTemplate<String, KafkaEvent> massageConsumer;
    private final KafkaEventHandlerRegistry handlerRegistry;

    public ChatKafkaConsumer(
            @Qualifier("outboxReactiveKafkaConsumerTemplate")
            ReactiveKafkaConsumerTemplate<String, KafkaEvent> outboxConsumer,
            @Qualifier("chatReactiveKafkaConsumerTemplate")
            ReactiveKafkaConsumerTemplate<String, KafkaEvent> massageConsumer,
            KafkaEventHandlerRegistry handlerRegistry) {
        this.outboxConsumer = outboxConsumer;
        this.massageConsumer = massageConsumer;
        this.handlerRegistry = handlerRegistry;
    }

    @PostConstruct
    public void listen() {
        // 로드밸런싱 컨슈머
        handleRecord(outboxConsumer);

        // fan-out 컨슈머
        handleRecord(massageConsumer);
    }

    private void handleRecord(ReactiveKafkaConsumerTemplate<String, KafkaEvent> consumer) {
        consumer.receive()
                .doOnNext(record -> {
                    KafkaEvent event = record.value();
                    KafkaEventHandler<KafkaEvent> handler = (KafkaEventHandler<KafkaEvent>) handlerRegistry.getHandler(event.getClass());
                    if (handler != null) {
                        handler.handle(event);
                    } else {
                        log.warn("No handler found for event type: {}", event.getClass().getSimpleName());
                    }
                    record.receiverOffset().acknowledge();
                })
                .doOnError(e -> log.error("Error in outbox consumer", e))
                .subscribe();
    }
}
