package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.dto.ChatMessageInfo;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatMessageNotificationHandler implements KafkaEventHandler<ChatMessageInfo> {

    private final ChatMessageService chatMessageService;

    @Override
    public Mono<Void> handle(ChatMessageInfo event) {
        return chatMessageService.broadcastMsg(event);
    }

    @Override
    public String getTopic() {
        return KafkaTopics.CHAT_MESSAGE_NOTIFICATION;
    }
}
