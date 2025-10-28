package com.example.chat_webflux.service;

import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatMessageService chatMessageService;
    private final OutboxEventService outboxEventService;
    private final ChatRoomRepository chatRoomRepository;

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
    @Transactional
    public Mono<Void> createRoom(String name) {
        ChatRoom newChatRoom = new ChatRoom(name);
        return chatRoomRepository.save(newChatRoom)
                .flatMap(savedRoom -> {
                    Map<String, Object> payloadMap = new HashMap<>();
                    payloadMap.put("id", savedRoom.getId());
                    payloadMap.put("name", savedRoom.getName());
                    return outboxEventService.saveOutboxEvent(KafkaTopics.CHAT_ROOM_CREATED, payloadMap)
                            .then();
                });
    }

    /**
     * 채팅방에 입장
     */
    public Mono<Void> enterRoom(Long roomId, String userId) {
        ChatMessage chatMessage = new ChatMessage(userId, roomId, userId + "님이 입장하셨습니다.");
        return chatMessageService.sendChatMessageKafkaEvent(chatMessage, true);
    }

    /**
     * 채팅방에서 퇴장
     */
    public Mono<Void> exitRoom(Long roomId, String userId) {
        ChatMessage chatMessage = new ChatMessage(userId, roomId, userId + "님이 퇴장하셨습니다.");
        return chatMessageService.sendChatMessageKafkaEvent(chatMessage, true);
    }
}
