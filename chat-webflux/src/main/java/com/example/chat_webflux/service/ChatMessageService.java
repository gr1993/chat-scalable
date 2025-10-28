package com.example.chat_webflux.service;

import com.example.chat_webflux.common.ChatRoomManager;
import com.example.chat_webflux.dto.ChatMessageInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.entity.MessageType;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.repository.ChatMessageRepository;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomManager chatRoomManager;
    private final ObjectMapper objectMapper;
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaSender;

    /**
     * CHAT_MESSAGE_CREATED, CHAT_MESSAGE_NOTIFICATION 두 토픽에 동시에 전송
     */
    public Mono<Void> sendChatMessageKafkaEvent(ChatMessage chatMessage, boolean isSystem) {
        String type = isSystem ? MessageType.system.name() : MessageType.user.name();

        return kafkaSender.sendTransactionally(
                Flux.just(
                        SenderRecord.create(
                                KafkaTopics.CHAT_MESSAGE_CREATED,
                                null,
                                null,
                                chatMessage.getRoomId().toString(),
                                chatMessage,
                                null
                        ),
                        SenderRecord.create(
                                KafkaTopics.CHAT_MESSAGE_NOTIFICATION,
                                null,
                                null,
                                chatMessage.getRoomId().toString(),
                                new ChatMessageInfo(chatMessage, type),
                                null
                        )
                )
        ).then();
    }

    public Mono<Void> saveChatMessage(ChatMessage chatMessage) {
        Long roomId = chatMessage.getRoomId();
        return chatRoomRepository.findById(roomId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않은 채팅방입니다.")))
                .flatMap(room ->
                    chatMessageRepository.save(chatMessage).then()
                );
    }

    public Mono<Void> broadcastMsg(ChatMessageInfo messageInfo) {
        try {
            Long roomId = messageInfo.getMessageId();
            WsJsonMessage<ChatMessageInfo> wsMsg = new WsJsonMessage<>(
                    "ROOM_MESSAGE",
                    "/topic/message/" + roomId,
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
