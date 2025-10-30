package com.example.chat_webflux.kafka;


import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatKafkaProducer {

    private final KafkaProperties kafkaProperties;

    @Value("${server.id}")
    private String serverId;

    /**
     * 요청 단위로 ReactiveKafkaProducerTemplate 생성
     */
    public ReactiveKafkaProducerTemplate<String, Object> createProducerForRequest() {
        Map<String, Object> producerProps = new HashMap<>(kafkaProperties.buildProducerProperties());

        // 요청마다 고유 트랜잭션 ID 생성
        String txId = "tx-" + serverId + "-" + UUID.randomUUID();
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, txId);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        SenderOptions<String, Object> senderOptions = SenderOptions.create(producerProps);
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}
