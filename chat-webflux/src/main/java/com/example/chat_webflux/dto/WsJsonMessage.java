package com.example.chat_webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsJsonMessage {
    private String type;           // SUBSCRIBE, UNSUBSCRIBE, SEND 등
    private String channel;        // 채널
    private String message;        // 실제 메시지
    private String roomId;
}
