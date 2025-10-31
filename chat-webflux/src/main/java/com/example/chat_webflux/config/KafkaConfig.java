package com.example.chat_webflux.config;

import com.example.chat_webflux.kafka.ChatKafkaProducerPool;
import com.example.chat_webflux.kafka.KafkaEvent;
import com.example.chat_webflux.kafka.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Value("${server.id}")
    private String serverId;

    @Bean
    public ChatKafkaProducerPool chatKafkaProducerPool(KafkaProperties props) {
        return new ChatKafkaProducerPool(props, serverId,10);
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
                .subscription(List.of(KafkaTopics.CHAT_USER_CREATED, KafkaTopics.CHAT_ROOM_CREATED, KafkaTopics.CHAT_MESSAGE_CREATED));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    // 채팅 메시지 중계용 (fan-out, 랜덤 group-id)
    @Bean
    public ReactiveKafkaConsumerTemplate<String, KafkaEvent> chatReactiveKafkaConsumerTemplate(
            KafkaProperties props) {
        Map<String, Object> consumerProps = new HashMap<>(props.buildConsumerProperties());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-server-" + UUID.randomUUID()); // 랜덤 그룹 아이디
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "chat-message-consumer-" + UUID.randomUUID());
        // group.id가 매번 랜덤으로 바뀌므로 Kafka는 새로운 소비자로 인식
        // 따라서 자동 오프셋 초기값(auto.offset.reset)을 'latest'로 설정해 최신 메시지부터 읽도록 설정
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        ReceiverOptions<String, KafkaEvent> receiverOptions = ReceiverOptions
                .<String, KafkaEvent>create(consumerProps)
                .subscription(List.of(KafkaTopics.CHAT_ROOM_NOTIFICATION, KafkaTopics.CHAT_MESSAGE_NOTIFICATION));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}