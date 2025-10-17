package com.example.chat_webflux.service.transaction;

import com.example.chat_webflux.entity.EventType;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.example.chat_webflux.repository.UserRepository;
import com.example.chat_webflux.service.ChatRoomService;
import com.example.chat_webflux.service.UserService;
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
    private ChatRoomService chatRoomService;

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
     * 사용자 정보가 저장되고 Outbox 테이블 저장
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
                .expectNextMatches(event -> EventType.USER_CREATED.getValue().equals(event.getEventType()))
                .verifyComplete();
    }

    /**
     * 채팅방 정보가 저장되고 Outbox 테이블 저장
     */
    @Test
    void createRoom_tx_성공() {
        // given
        String roomName = "test-room";

        // when
        chatRoomService.createRoom(roomName).block();

        // then
        StepVerifier.create(chatRoomRepository.findAll())
                .expectNextMatches(room -> roomName.equals(room.getName()))
                .verifyComplete();

        StepVerifier.create(outboxEventRepository.findAll())
                .expectNextMatches(event -> EventType.CHAT_ROOM_CREATED.getValue().equals(event.getEventType()))
                .verifyComplete();
    }
}
