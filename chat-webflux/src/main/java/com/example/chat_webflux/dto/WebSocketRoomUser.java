package com.example.chat_webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketRoomUser {
    private Long roomId;
    private String userId;
}
