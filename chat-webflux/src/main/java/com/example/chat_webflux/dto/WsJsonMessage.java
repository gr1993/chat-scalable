package com.example.chat_webflux.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WsJsonMessage {
    private String type;           // SUBSCRIBE, UNSUBSCRIBE, SEND 등
    private String destination;    // 목적지
    private String message;        // 실제 메시지
    private String roomId;

    public WsJsonMessage(String type, String destination) {
        this.type = type;
        this.destination = destination;
    }
}
