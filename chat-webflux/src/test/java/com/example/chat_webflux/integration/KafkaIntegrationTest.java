package com.example.chat_webflux.integration;

import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.kafka.ChatKafkaProducerPool;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.service.ChatMessageService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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
    topics = {
            KafkaTopics.CHAT_USER_CREATED,
            KafkaTopics.CHAT_MESSAGE_CREATED,
            KafkaTopics.CHAT_MESSAGE_NOTIFICATION
    }
)
@ExtendWith(EmbeddedRedisExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class KafkaIntegrationTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatKafkaProducerPool producerPool;

    private ReactiveKafkaConsumerTemplate<String, Object> kafkaConsumer;

    @Autowired
    private KafkaProperties kafkaProperties;


    /**
     * KafkaConfig에서 등록된 Consumer는 이미 구독중이므로 테스트용 Consumer를 별도로 생성
     */
    @BeforeEach
    public void setup() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());

        ReceiverOptions<String, Object> options = ReceiverOptions.<String, Object>create(props)
                .subscription(List.of(KafkaTopics.CHAT_USER_CREATED, KafkaTopics.CHAT_MESSAGE_CREATED, KafkaTopics.CHAT_MESSAGE_NOTIFICATION));

        kafkaConsumer = new ReactiveKafkaConsumerTemplate<>(options);
    }


    @Test
    void send_성공() {
        // given
        ChatUser chatUser = new ChatUser("park");

        // when
        Mono<Void> sendMono = producerPool.sendKafkaEvent(List.of(
                SenderRecord.create(
                        KafkaTopics.CHAT_MESSAGE_CREATED,
                        null,
                        null,
                        chatUser.getId(),
                        chatUser,
                        null
                )
        ));
        Mono<ChatUser> result = kafkaConsumer.receiveAutoAck()
                .map(ConsumerRecord::value)
                .cast(ChatUser.class)
                .next()
                .delaySubscription(sendMono);

        // then
        ChatUser received = result.block(Duration.ofSeconds(5));
        assertNotNull(received);
        assertEquals("park", received.getId());
    }

    /**
     * 여러 토픽에 메시지 발행(트랜잭션)
     */
    @Test
    public void sendKafkaTx_성공() {
        // given
        ChatMessage chatMessage = new ChatMessage("park", 1L, "안녕하세요~");

        // when
        Mono<Void> sendMono = chatMessageService.sendChatMessageKafkaEvent(chatMessage, false);

        // then
        Flux<ConsumerRecord<String, Object>> consumedFlux = kafkaConsumer.receiveAutoAck()
                .filter(record -> record.topic().equals(KafkaTopics.CHAT_MESSAGE_CREATED)
                        || record.topic().equals(KafkaTopics.CHAT_MESSAGE_NOTIFICATION))
                .take(2);

        StepVerifier.create(sendMono.thenMany(consumedFlux))
                .recordWith(ArrayList::new)
                .expectNextCount(2)
                .consumeRecordedWith(records -> {
                    Set<String> topics = records.stream()
                            .map(ConsumerRecord::topic)
                            .collect(Collectors.toSet());
                    assertTrue(topics.contains(KafkaTopics.CHAT_MESSAGE_CREATED));
                    assertTrue(topics.contains(KafkaTopics.CHAT_MESSAGE_NOTIFICATION));
                })
                .verifyComplete();
    }

    /**
     * 여러 토픽에 발행 시 실패 시나리오 테스트(트랜잭션)
     */
    @Test
    public void sendKafkaTx_실패() {
        // given
        ChatMessage chatMessage = new ChatMessage("park", 1L, "안녕하세요~");
        Object invalidValue = new Object() {  // Jackson 직렬화 불가
            private void someMethod() {}
        };

        List<SenderRecord<String, Object, Object>> recordList = List.of(
                SenderRecord.create(KafkaTopics.CHAT_MESSAGE_CREATED, null, null,
                        chatMessage.getRoomId().toString(), chatMessage, null),
                SenderRecord.create(KafkaTopics.CHAT_MESSAGE_NOTIFICATION, null, null,
                        chatMessage.getRoomId().toString(), invalidValue, null)
        );

        // then
        Mono<Void> sendMono = producerPool.sendKafkaEvent(recordList);

        // then
        StepVerifier.create(sendMono)
                .expectError()
                .verify();

        StepVerifier.create(kafkaConsumer.receiveAutoAck()
                        .take(Duration.ofSeconds(3)))
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }
}
