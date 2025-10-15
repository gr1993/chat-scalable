package com.example.chat_webflux.service;

import com.example.chat_webflux.common.ChatRoomManager;
import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomManager chatRoomManager;
    private final ObjectMapper objectMapper;

    /**
     * 전체 채팅방 리스트 조회
     */
    public Mono<List<ChatRoomInfo>> getRoomList() {
        return chatRoomRepository.findAll()
                .map(ChatRoomInfo::new)
                .collectList();
    }

    /**
     * 채팅방 만들기
     */
    public Mono<Void> createRoom(String name) {
        ChatRoom newChatRoom = new ChatRoom(name);
        return chatRoomRepository.save(newChatRoom)
                .flatMap(savedRoom -> {

                    // 채팅방 생성을 구독자들에게 알림
                    try {
                        ChatRoomInfo chatRoomInfo = new ChatRoomInfo(savedRoom);
                        WsJsonMessage<ChatRoomInfo> wsMsg = new WsJsonMessage<>("ROOM_CREATED", "/topic/rooms", chatRoomInfo);

                        Sinks.Many<String> serverSinks = chatRoomManager.getChatServerSinks();
                        serverSinks.tryEmitNext(objectMapper.writeValueAsString(wsMsg));
                    } catch (JsonProcessingException ex) {
                        return Mono.error(ex);
                    }

                    return Mono.empty();
                });
    }
}
