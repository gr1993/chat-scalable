package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxEventServiceTest {

    @InjectMocks
    private OutboxEventService outboxEventService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReactiveKafkaProducerTemplate<String, Object> kafkaSender;


    @Test
    void saveOutboxEvent_성공() throws Exception {
        // given
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("id", 1L);
        payloadMap.put("name", "테스트방");

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenReturn(Mono.empty());

        // when
        outboxEventService.saveOutboxEvent(
                KafkaTopics.CHAT_ROOM_CREATED,
                payloadMap
        ).block();

        // then
        verify(objectMapper).writeValueAsString(any());
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }
}