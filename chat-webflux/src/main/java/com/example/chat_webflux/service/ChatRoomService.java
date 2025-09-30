package com.example.chat_webflux.service;

import com.example.chat_webflux.common.ChatRoomManager;
import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomManager chatRoomManager;
    private final ObjectMapper objectMapper;

    /**
     * 채팅방 만들기
     */
    public Mono<Void> createRoom(String name) {
        ChatRoom newChatRoom = new ChatRoom(name);
        chatRoomRepository.save(newChatRoom);

        // 채팅방 생성을 구독자들에게 알림
        try {
            Sinks.Many<String> serverSinks = chatRoomManager.getChatServerSinks();
            serverSinks.tryEmitNext(objectMapper.writeValueAsString(new ChatRoomInfo(newChatRoom)));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        return Mono.empty();
    }
}
