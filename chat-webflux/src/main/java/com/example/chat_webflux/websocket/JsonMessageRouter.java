package com.example.chat_webflux.websocket;

import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.service.SubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class JsonMessageRouter {

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    public void handleJsonMessage(Sinks.Many<String> sessionSink, WebSocketSession session, String jsonMessage) {
        try {
            WsJsonMessage wsJsonMessage = objectMapper.readValue(jsonMessage, WsJsonMessage.class);
            switch (wsJsonMessage.getType()) {
                case "SEND":
                    break;
                case "SUBSCRIBE":
                    handleSubscribe(sessionSink, session, wsJsonMessage);
                    break;
                case "UNSUBSCRIBE":
                    handleUnsubscribe(session, wsJsonMessage);
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleSubscribe(Sinks.Many<String> sessionSink, WebSocketSession session, WsJsonMessage wsJsonMessage) {
        String sessionId = session.getId();
        String destination = wsJsonMessage.getDestination();

        if (destination.startsWith("/topic/rooms")) {
            subscriptionService.subscribeRoomCreate(sessionSink, destination, sessionId);
        }
    }

    private void handleUnsubscribe(WebSocketSession session, WsJsonMessage wsJsonMessage) {
        if (wsJsonMessage != null) {
            String sessionId = session.getId();
            String destination = wsJsonMessage.getDestination();
            subscriptionService.unSubscribe(sessionId, destination);
        }
    }
}
