package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.example.chat_webflux.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    /**
     * 사용자 입장 처리
     */
    @Transactional
    public Mono<Void> enterUser(String userId) {
        return userRepository.existsById(userId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("이미 존재하는 아이디입니다."));
                    }
                    return userRepository.save(new ChatUser(userId));
                })
                .flatMap(savedUser -> {
                    Map<String, Object> payloadMap = new HashMap<>();
                    payloadMap.put("id", savedUser.getId());

                    OutboxEvent outboxEvent = new OutboxEvent(UUID.randomUUID());
                    outboxEvent.setEventType("chatRoom.created");
                    outboxEvent.setEventVersion("v1.0");

                    try {
                        String payloadJson = objectMapper.writeValueAsString(payloadMap);
                        outboxEvent.setPayload(payloadJson);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                    return outboxEventRepository.save(outboxEvent).then();
                });
    }
}
