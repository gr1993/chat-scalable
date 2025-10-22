package com.example.chat_webflux.config;

import com.example.chat_webflux.kafka.KafkaEvent;
import com.example.chat_webflux.kafka.KafkaTopics;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.List;

@Configuration
public class KafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(
            KafkaProperties props) {
        return new ReactiveKafkaProducerTemplate<>(
                SenderOptions.create(props.buildProducerProperties())
        );
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, KafkaEvent> reactiveKafkaConsumerTemplate(
            KafkaProperties props) {
        ReceiverOptions<String, KafkaEvent> receiverOptions = ReceiverOptions
                .<String, KafkaEvent>create(props.buildConsumerProperties())
                .subscription(List.of(KafkaTopics.CHAT_USER_CREATED));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}