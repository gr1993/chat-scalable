package com.example.chat_webflux.dto;

import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.kafka.KafkaEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageInfo implements KafkaEvent {
    private Long messageId;
    private String senderId;
    private String message;
    private String sendDt;
    private String type;

    public ChatMessageInfo(ChatMessage chatMessage, String type) {
        this.messageId = chatMessage.getId();
        this.senderId = chatMessage.getSenderId();
        this.message = chatMessage.getMessage();
        this.sendDt = chatMessage.getSendDt().toString();
        this.type = type;
    }
}
