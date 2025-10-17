package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.EventType;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.example.chat_webflux.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionSuccessTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll().block();
        chatRoomRepository.deleteAll().block();
        outboxEventRepository.deleteAll().block();
    }

    /**
     * 사용자 저장되고 Outbox 테이블 저장 실패 시나리오
     */
    @Test
    void enterUser_tx_성공() {
        // given
        String userId = "test-user";

        // when
        userService.enterUser(userId).block();

        // then
        StepVerifier.create(userRepository.findById(userId))
                .expectNextMatches(user -> user.getId().equals(userId))
                .verifyComplete();

        StepVerifier.create(outboxEventRepository.findAll())
                .expectNextMatches(event -> EventType.CHAT_ROOM_CREATED.getValue().equals(event.getEventType()))
                .verifyComplete();
    }
}
