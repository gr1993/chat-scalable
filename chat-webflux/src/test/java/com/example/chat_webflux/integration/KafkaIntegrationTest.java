package com.example.chat_webflux.integration;

import com.example.chat_webflux.entity.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

/**
 * @EmbeddedKafka
 * 테스트 실행 시 자체 Kafka 브로커를 띄움
 * Kafka 설정을 무시하고 override (bootstrap-servers)
 */
@EmbeddedKafka(
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:39092",
        "port=39092",
        "security.protocol=PLAINTEXT"
    },
    partitions = 3,
    topics = { "chat-topic" }
)
@SpringBootTest
public class KafkaIntegrationTest {

    @Autowired
    private ReactiveKafkaProducerTemplate<String, Object> kafkaSender;

    @Test
    public void send() {
        ChatMessage message = new ChatMessage(
                "tester",
                1L,
                "Hello, Kafka!"
        );

        kafkaSender.send("chat-topic", message)
                .doOnSuccess(result -> System.out.println("Sent: " + result.recordMetadata()))
                .doOnError(error -> System.err.println("Failed to send: " + error.getMessage()))
                .block();
    }
}
