package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OutboxEventService outboxEventService;
    private final UserRepository userRepository;

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
                    return outboxEventService.saveOutboxEvent(KafkaTopics.CHAT_USER_CREATED, payloadMap)
                            .then();
                });
    }
}
