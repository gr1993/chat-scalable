package com.example.chat_webflux.service.transaction;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionFailTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @MockitoBean
    private OutboxEventRepository outboxEventRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll().block();
        chatRoomRepository.deleteAll().block();
    }

    /**
     * 사용자 정보가 저장되고 Outbox 테이블 저장 실패 시나리오(롤백 확인)
     */
    @Test
    void enterUser_tx_실패() {
        // given
        String userId = "test-user";
        when(outboxEventRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("outbox 저장 실패")));

        // when
        StepVerifier.create(userService.enterUser(userId))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("outbox 저장 실패"))
                .verify();

        // then
        StepVerifier.create(userRepository.findById(userId))
                .expectNextCount(0)
                .verifyComplete();
    }

    /**
     * 채팅방 정보가 저장되고 Outbox 테이블 저장 실패 시나리오(롤백 확인)
     */
    @Test
    void createRoom_tx_실패() {
        // given
        String roomName = "test-room";
        when(outboxEventRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("outbox 저장 실패")));

        // when
        StepVerifier.create(chatRoomService.createRoom(roomName))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("outbox 저장 실패"))
                .verify();

        // then
        StepVerifier.create(userRepository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }
}
