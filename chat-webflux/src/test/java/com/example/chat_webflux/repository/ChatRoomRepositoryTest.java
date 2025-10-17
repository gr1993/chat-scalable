package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.ChatRoom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll().block();
    }

    @Test
    void save_성공() {
        // given
        ChatRoom chatRoom = new ChatRoom("park");

        // when
        chatRoomRepository.save(chatRoom).block();

        // then
        Flux<ChatRoom> roomList = chatRoomRepository.findAll();
        StepVerifier.create(roomList)
                .assertNext(room -> {
                    assertEquals(chatRoom.getId(), room.getId());
                    assertEquals(chatRoom.getName(), room.getName());
                })
                .verifyComplete();
    }

    @Test
    void findAll_성공() {
        // given
        chatRoomRepository.save(new ChatRoom("park")).block();
        chatRoomRepository.save(new ChatRoom("kang")).block();

        // when
        Flux<ChatRoom> roomList = chatRoomRepository.findAll();

        // then
        StepVerifier.create(roomList)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    @Test
    void findById_성공() {
        // given
        ChatRoom searchRoom = new ChatRoom("kang");
        chatRoomRepository.save(new ChatRoom("park")).block();
        chatRoomRepository.save(searchRoom).block();

        // when
        Mono<ChatRoom> roomMono = chatRoomRepository.findById(searchRoom.getId());

        // then
        StepVerifier.create(roomMono)
                .assertNext(room -> {
                    assertEquals(searchRoom.getId(), room.getId());
                    assertEquals(searchRoom.getName(), room.getName());
                })
                .verifyComplete();
    }

    @Test
    void update_성공() {
        // given
        ChatRoom searchRoom = new ChatRoom("park");
        chatRoomRepository.save(searchRoom).block();
        ChatRoom updateRoom = chatRoomRepository.findById(searchRoom.getId()).block();

        // when
        String updatedName = "kang";
        updateRoom.setName(updatedName);
        chatRoomRepository.save(updateRoom).block();

        // then
        ChatRoom roomInfo = chatRoomRepository.findById(updateRoom.getId()).block();
        assertEquals(updatedName, roomInfo.getName());
    }
}
