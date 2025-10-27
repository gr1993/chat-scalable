package com.example.chat_webflux.config;

import com.example.chat_webflux.kafka.KafkaEvent;
import com.example.chat_webflux.kafka.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(
            KafkaProperties props) {
        return new ReactiveKafkaProducerTemplate<>(
                SenderOptions.create(props.buildProducerProperties())
        );
    }

    // Outbox 이벤트 처리용 (로드밸런싱, 고정 group-id)
    @Bean
    public ReactiveKafkaConsumerTemplate<String, KafkaEvent> outboxReactiveKafkaConsumerTemplate(
            KafkaProperties props) {
        // 각 Consumer 끼리 Client-ID가 달라야 각자 구독이 가능해진다.
        Map<String, Object> consumerProps = new HashMap<>(props.buildConsumerProperties());
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "chat-outbox-consumer");

        ReceiverOptions<String, KafkaEvent> receiverOptions = ReceiverOptions
                .<String, KafkaEvent>create(consumerProps)
                .subscription(List.of(KafkaTopics.CHAT_USER_CREATED, KafkaTopics.CHAT_ROOM_CREATED));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    // 채팅 메시지 중계용 (fan-out, 랜덤 group-id)
    @Bean
    public ReactiveKafkaConsumerTemplate<String, KafkaEvent> chatReactiveKafkaConsumerTemplate(
            KafkaProperties props) {
        Map<String, Object> consumerProps = new HashMap<>(props.buildConsumerProperties());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-server-" + UUID.randomUUID()); // 랜덤 그룹 아이디
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "chat-message-consumer-" + UUID.randomUUID());

        ReceiverOptions<String, KafkaEvent> receiverOptions = ReceiverOptions
                .<String, KafkaEvent>create(consumerProps)
                .subscription(List.of(KafkaTopics.CHAT_MESSAGE_CREATED));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}