package com.example.chat_webflux.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table
@NoArgsConstructor
public class ChatRoom {

    @Id
    private Long id;
    private String name;
    private LocalDateTime createDt;

    public ChatRoom(String name) {
        this.name = name;
        this.createDt = LocalDateTime.now();
    }
}
