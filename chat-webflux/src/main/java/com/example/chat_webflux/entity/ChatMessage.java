package com.example.chat_webflux.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table
@NoArgsConstructor
public class ChatMessage {

    @Id
    private Long id;
    private String senderId;
    private Long roomId;
    private String message;
    private LocalDateTime sendDt;

    public ChatMessage(String senderId, Long roomId, String message) {
        this.senderId = senderId;
        this.roomId = roomId;
        this.message = message;
        this.sendDt = LocalDateTime.now();
    }
}
