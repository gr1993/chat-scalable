package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.kafka.KafkaTopics;
import com.example.chat_webflux.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatMessageCreatedHandler implements KafkaEventHandler<ChatMessage> {

    private final ChatMessageService chatMessageService;

    @Override
    public Mono<Void> handle(ChatMessage event) {
        return chatMessageService.saveChatMessage(event);
    }

    @Override
    public String getTopic() {
        return KafkaTopics.CHAT_MESSAGE_CREATED;
    }
}
