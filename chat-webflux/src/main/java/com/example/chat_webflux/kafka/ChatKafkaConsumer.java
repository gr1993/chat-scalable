package com.example.chat_webflux.kafka;

import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.ChatUser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatKafkaConsumer {

    private final ReactiveKafkaConsumerTemplate<String, KafkaEvent> outboxConsumer;

    public ChatKafkaConsumer(
            @Qualifier("outboxReactiveKafkaConsumerTemplate")
            ReactiveKafkaConsumerTemplate<String, KafkaEvent> outboxConsumer) {
        this.outboxConsumer = outboxConsumer;
    }

    @PostConstruct
    public void listen() {
        outboxConsumer.receive()
                .doOnNext(record -> {
                    KafkaEvent event = record.value();

                    if (event instanceof ChatUser user) {

                    } else if (event instanceof ChatMessage chat) {

                    }

                    record.receiverOffset().acknowledge();
                })
                .subscribe();
    }
}
