package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.entity.ChatUser;
import org.springframework.stereotype.Component;

@Component
public class ChatUserHandler implements KafkaEventHandler<ChatUser> {

    @Override
    public void handle(ChatUser event) {

    }

    @Override
    public Class<ChatUser> getEventType() {
        return ChatUser.class;
    }
}
