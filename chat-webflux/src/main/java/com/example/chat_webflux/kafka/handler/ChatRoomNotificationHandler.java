package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.common.ChatRoomManager;
import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class ChatRoomNotificationHandler implements KafkaEventHandler<ChatRoom> {

    private final ChatRoomManager chatRoomManager;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ChatRoom event) {
        return Mono.fromRunnable(() -> {
            ChatRoomInfo chatRoomInfo = new ChatRoomInfo(event);
            WsJsonMessage<ChatRoomInfo> wsMsg = new WsJsonMessage<>("ROOM_CREATED", "/topic/rooms", chatRoomInfo);

            Sinks.Many<String> serverSinks = chatRoomManager.getChatServerSinks();
            try {
                // 채팅방 생성을 구독자들에게 알림(웹소켓)
                serverSinks.tryEmitNext(objectMapper.writeValueAsString(wsMsg));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public String getTopic() {
        return KafkaTopics.CHAT_ROOM_NOTIFICATION;
    }
}
