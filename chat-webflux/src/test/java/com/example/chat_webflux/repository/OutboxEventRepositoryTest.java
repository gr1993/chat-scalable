package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.EventType;
import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.entity.OutboxEventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        outboxEventRepository.deleteAll().block();
    }

    @Test
    void save_성공() throws Exception {
        // given
        OutboxEvent outboxEvent = getChatRoomOutboxEvent(1L, "테스트 채팅방");

        // when
        outboxEventRepository.save(outboxEvent).block();

        // then
        Flux<OutboxEvent> eventList = outboxEventRepository.findAll();
        StepVerifier.create(eventList)
                .assertNext(event -> {
                    assertEquals(outboxEvent.getEventId(), event.getEventId());
                    assertEquals(outboxEvent.getEventType(), event.getEventType());
                    assertEquals(outboxEvent.getEventVersion(), event.getEventVersion());
                    assertEquals(outboxEvent.getPayload(), event.getPayload());
                    assertEquals(OutboxEventStatus.PENDING.name(), event.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void findAll_성공() throws Exception {
        // given
        OutboxEvent outboxEvent1 = getChatRoomOutboxEvent(1L, "테스트 채팅방");
        OutboxEvent outboxEvent2 = getChatRoomOutboxEvent(2L, "테스트 채팅방2");
        outboxEventRepository.save(outboxEvent1).block();
        outboxEventRepository.save(outboxEvent2).block();

        // when
        Flux<OutboxEvent> eventList = outboxEventRepository.findAll();

        // then
        StepVerifier.create(eventList)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    @Test
    void findByStatus_성공() throws Exception {
        // given
        OutboxEvent outboxEvent1 = getChatRoomOutboxEvent(1L, "테스트 채팅방");
        outboxEvent1.setStatus(OutboxEventStatus.SENT.name());
        OutboxEvent outboxEvent2 = getChatRoomOutboxEvent(2L, "테스트 채팅방2");
        outboxEventRepository.save(outboxEvent1).block();
        outboxEventRepository.save(outboxEvent2).block();

        // when
        Flux<OutboxEvent> eventList = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING.name());

        // then
        StepVerifier.create(eventList)
                .assertNext(event -> {
                    assertEquals(outboxEvent2.getEventId(), event.getEventId());
                    assertEquals(outboxEvent2.getEventType(), event.getEventType());
                    assertEquals(outboxEvent2.getEventVersion(), event.getEventVersion());
                    assertEquals(outboxEvent2.getPayload(), event.getPayload());
                    assertEquals(OutboxEventStatus.PENDING.name(), event.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void update_성공() throws Exception {
        // given
        OutboxEvent outboxEvent = getChatRoomOutboxEvent(1L, "테스트 채팅방");
        outboxEventRepository.save(outboxEvent).block();
        OutboxEvent updateEvent = outboxEventRepository.findById(outboxEvent.getEventId()).block();

        // when
        String updatedEventType = "kang";
        updateEvent.setEventType(updatedEventType);
        outboxEventRepository.save(updateEvent).block();

        // then
        OutboxEvent eventInfo = outboxEventRepository.findById(outboxEvent.getEventId()).block();
        assertEquals(updatedEventType, eventInfo.getEventType());
    }

    private OutboxEvent getChatRoomOutboxEvent(long id, String name) throws Exception {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("id", id);
        payloadMap.put("name", name);

        OutboxEvent outboxEvent = new OutboxEvent(UUID.randomUUID());
        outboxEvent.setEventType(EventType.CHAT_ROOM_CREATED.getValue());
        outboxEvent.setEventVersion("v1.0");
        String payloadJson = objectMapper.writeValueAsString(payloadMap);
        outboxEvent.setPayload(payloadJson);
        return outboxEvent;
    }
}
