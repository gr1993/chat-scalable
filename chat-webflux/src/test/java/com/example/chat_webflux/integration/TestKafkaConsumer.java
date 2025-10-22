package com.example.chat_webflux.integration;

import com.example.chat_webflux.kafka.KafkaEvent;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TestKafkaConsumer {

    private final BlockingQueue<KafkaEvent> receivedMessages = new LinkedBlockingQueue<>();

    public TestKafkaConsumer(ReactiveKafkaConsumerTemplate<String, KafkaEvent> kafkaConsumer) {
        kafkaConsumer.receive()
                .doOnNext(record -> {
                    receivedMessages.add(record.value());
                    record.receiverOffset().acknowledge();
                })
                .subscribe();
    }

    public KafkaEvent take() throws InterruptedException {
        return receivedMessages.poll(10, TimeUnit.SECONDS);
    }
}