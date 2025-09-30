package com.example.chat_webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private Long id;
    private String senderId;
    private Long roomId;
    private String message;
    private LocalDateTime sendDt;
}
