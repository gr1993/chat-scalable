package com.example.chat_webflux.integration;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.kafka.KafkaEvent;
import com.example.chat_webflux.kafka.KafkaTopics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.junit.jupiter.api.Assertions.*;

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
    topics = { KafkaTopics.CHAT_USER_CREATED }
)
@SpringBootTest
public class KafkaIntegrationTest {

    @Autowired
    private ReactiveKafkaProducerTemplate<String, Object> kafkaSender;

    @Autowired
    private ReactiveKafkaConsumerTemplate<String, KafkaEvent> kafkaConsumer;
    private TestKafkaConsumer testKafkaConsumer;

    @BeforeEach
    public void setup() {
        testKafkaConsumer = new TestKafkaConsumer(kafkaConsumer);
    }

    @Test
    public void send_성공() throws Exception {
        // given
        Thread.sleep(4000);
        ChatUser chatUser = new ChatUser("park");

        // when
        kafkaSender.send(KafkaTopics.CHAT_USER_CREATED, chatUser)
                .doOnSuccess(result -> System.out.println("Sent: " + result.recordMetadata()))
                .doOnError(error -> System.err.println("Failed to send: " + error.getMessage()))
                .block();

        // then
        KafkaEvent received = testKafkaConsumer.take();
        assertNotNull(received);
        assertInstanceOf(ChatUser.class, received);
        ChatUser receivedChatUser = (ChatUser) received;
        assertEquals(chatUser.getId(), receivedChatUser.getId());
    }
}
