package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.integration.EmbeddedRedisExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EmbeddedRedisExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll().block();
        chatRoomRepository.deleteAll().block();
    }

    @Test
    void save_성공() {
        // given
        ChatRoom chatRoom = new ChatRoom("myRoom");
        chatRoomRepository.save(chatRoom).block();

        ChatMessage chatMessage = new ChatMessage("park", chatRoom.getId(), "hello");

        // when
        chatMessageRepository.save(chatMessage).block();

        // then
        Flux<ChatMessage> msgList = chatMessageRepository.findAll();
        StepVerifier.create(msgList)
                .assertNext(message -> {
                    assertEquals(chatMessage.getId(), message.getId());
                    assertEquals(chatMessage.getSenderId(), message.getSenderId());
                    assertEquals(chatMessage.getRoomId(), message.getRoomId());
                    assertEquals(chatMessage.getMessage(), message.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void findAll_성공() {
        // given
        ChatRoom chatRoom = new ChatRoom("myRoom");
        chatRoomRepository.save(chatRoom).block();

        chatMessageRepository.save(new ChatMessage("park", chatRoom.getId(), "hello")).block();
        chatMessageRepository.save(new ChatMessage("kang", chatRoom.getId(), "world")).block();

        // when
        Flux<ChatMessage> msgList = chatMessageRepository.findAll();

        // then
        StepVerifier.create(msgList)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    @Test
    void findById_성공() {
        // given
        ChatRoom chatRoom = new ChatRoom("myRoom");
        chatRoomRepository.save(chatRoom).block();

        ChatMessage searchMsg = new ChatMessage("kang", chatRoom.getId(), "world");
        chatMessageRepository.save(new ChatMessage("park", chatRoom.getId(), "hello")).block();
        chatMessageRepository.save(searchMsg).block();

        // when
        Mono<ChatMessage> msgMono = chatMessageRepository.findById(searchMsg.getId());

        // then
        StepVerifier.create(msgMono)
                .assertNext(msg -> {
                    assertEquals(searchMsg.getId(), msg.getId());
                    assertEquals(searchMsg.getSenderId(), msg.getSenderId());
                    assertEquals(searchMsg.getRoomId(), msg.getRoomId());
                    assertEquals(searchMsg.getMessage(), msg.getMessage());
                })
                .verifyComplete();
    }
}
