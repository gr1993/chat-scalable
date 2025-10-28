package com.example.chat_webflux.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KafkaEvent를 구현하면 JSON 전송 시 오류가 발생해 DTO 분리(웹소켓용)
 */
@Data
@NoArgsConstructor
public class ChatMessageWs {
    private Long messageId;
    private String senderId;
    private String message;
    private String sendDt;
    private String type;

    public ChatMessageWs(ChatMessageInfo chatMessageInfo) {
        this.messageId = chatMessageInfo.getMessageId();
        this.senderId = chatMessageInfo.getSenderId();
        this.message = chatMessageInfo.getMessage();
        this.sendDt = chatMessageInfo.getSendDt();
        this.type = chatMessageInfo.getType();
    }
}
