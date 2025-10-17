package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public Mono<OutboxEvent> saveOutboxEvent(String eventType, Map<String, Object> payloadMap) {
        OutboxEvent outboxEvent = new OutboxEvent(eventType);

        try {
            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            outboxEvent.setPayload(payloadJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        return outboxEventRepository.save(outboxEvent);
    }
}
