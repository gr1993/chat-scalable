package com.example.chat_webflux.kafka;

import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.ChatUser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatKafkaConsumer {

    private final ReactiveKafkaConsumerTemplate<String, KafkaEvent> consumer;

    @PostConstruct
    public void listen() {
        consumer.receive()
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
