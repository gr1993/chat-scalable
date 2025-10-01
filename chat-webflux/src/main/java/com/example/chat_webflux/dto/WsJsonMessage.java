package com.example.chat_webflux.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WsJsonMessage<T> {
    private String type;           // SUBSCRIBE, UNSUBSCRIBE, SEND 등
    private String destination;    // 목적지
    private T data;

    public WsJsonMessage(String type, String destination) {
        this.type = type;
        this.destination = destination;
        this.data = null;
    }
    public WsJsonMessage(String type, String destination, T data) {
        this.type = type;
        this.destination = destination;
        this.data = data;
    }
}
