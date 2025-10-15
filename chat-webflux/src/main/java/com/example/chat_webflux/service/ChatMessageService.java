package com.example.chat_webflux.service;

import com.example.chat_webflux.common.ChatRoomManager;
import com.example.chat_webflux.dto.ChatMessageInfo;
import com.example.chat_webflux.dto.SendMessageInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.MessageType;
import com.example.chat_webflux.repository.ChatMessageRepository;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomManager chatRoomManager;
    private final ObjectMapper objectMapper;

    public Mono<Void> sendMessageToRoom(SendMessageInfo sendMessageInfo) {
        return sendMessageToRoom(sendMessageInfo, false);
    }

    public Mono<Void> sendMessageToRoom(SendMessageInfo sendMessageInfo, boolean isSystem) {
        Long roomId = sendMessageInfo.getRoomId();
        return chatRoomRepository.findById(roomId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않은 채팅방입니다.")))
                .flatMap(room -> {
                    ChatMessage chatMessage = new ChatMessage(
                            sendMessageInfo.getUserId(),
                            sendMessageInfo.getRoomId(),
                            sendMessageInfo.getMessage()
                    );
                    return chatMessageRepository.save(chatMessage);
                })
                .flatMap(savedMsg  -> {
                    // 채팅방에 새 메세지를 구독자들에게 알림
                    return broadcastMsg(roomId, savedMsg, isSystem);
                });
    }

    private Mono<Void> broadcastMsg(Long roomId, ChatMessage chatMessage, boolean isSystem) {
        try {
            ChatMessageInfo messageInfo = new ChatMessageInfo(
                    chatMessage,
                    isSystem ? MessageType.system.name() : MessageType.user.name()
            );
            WsJsonMessage<ChatMessageInfo> wsMsg = new WsJsonMessage<>(
                    "ROOM_MESSAGE",
                    "/topic/message/" + chatMessage.getRoomId(),
                    messageInfo
            );

            Sinks.Many<String> roomSink = chatRoomManager.getRoomSink(roomId.toString());
            roomSink.tryEmitNext(objectMapper.writeValueAsString(wsMsg));

            return Mono.empty();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
