package com.example.chat_webflux.websocket;

import com.example.chat_webflux.dto.WsJsonMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class JsonMessageRouter {

    private final ObjectMapper objectMapper;

    public void handleJsonMessage(Sinks.Many<String> sessionSink, WebSocketSession session, String jsonMessage) {
        try {
            WsJsonMessage wsJsonMessage = objectMapper.readValue(jsonMessage, WsJsonMessage.class);
            switch (wsJsonMessage.getType()) {
                case "SEND":
                    break;
                case "SUBSCRIBE":
                    break;
                case "UNSUBSCRIBE":
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
