package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.kafka.ChatKafkaProducer;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private ChatKafkaProducer chatKafkaProducer;;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    ReactiveKafkaProducerTemplate<String, Object> producer;

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

    @Test
    void checkOutboxAndPublish_성공() throws Exception {
        // given
        String id = "park";
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("id", id);
        OutboxEvent outboxEvent = getChatUserOutboxEvent(payloadMap);

        when(outboxEventRepository.findByStatus(any(String.class)))
                .thenReturn(Flux.just(outboxEvent));
        when(objectMapper.readValue(any(String.class), any(TypeReference.class)))
                .thenReturn(payloadMap);
        when(chatKafkaProducer.createProducerForRequest())
                .thenReturn(producer);
        when(producer.sendTransactionally(any(Flux.class)))
                .thenReturn(Flux.empty());
        when(outboxEventRepository.updateStatus(any(UUID.class), any(String.class)))
                .thenReturn(Mono.empty());

        // when
        outboxEventService.checkOutboxAndPublish().block();

        // then
        verify(producer).sendTransactionally(any(Flux.class));
    }

    @Test
    void checkOutboxAndPublish_조회결과없음() throws Exception {
        // given
        when(outboxEventRepository.findByStatus(any(String.class)))
                .thenReturn(Flux.empty());

        // when
        outboxEventService.checkOutboxAndPublish().block();

        // then
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
        verify(producer, never()).send(anyString(), any());
    }

    private OutboxEvent getChatUserOutboxEvent(Map<String, Object> payloadMap) throws Exception {
        ObjectMapper realObjectMapper = new ObjectMapper();

        OutboxEvent outboxEvent = new OutboxEvent(UUID.randomUUID());
        outboxEvent.setEventType(KafkaTopics.CHAT_USER_CREATED);
        outboxEvent.setEventVersion("v1.0");
        String payloadJson = realObjectMapper.writeValueAsString(payloadMap);
        outboxEvent.setPayload(payloadJson);
        return outboxEvent;
    }
}