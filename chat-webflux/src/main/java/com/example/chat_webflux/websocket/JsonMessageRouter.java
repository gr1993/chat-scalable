package com.example.chat_webflux.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class JsonMessageRouter {

    public void handleJsonMessage(Sinks.Many<String> sessionSink, WebSocketSession session, String stompMessage) {

    }
}
