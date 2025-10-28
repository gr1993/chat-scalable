package com.example.chat_webflux.websocket;

import com.example.chat_webflux.dto.SendMessageInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatMessage;
import com.example.chat_webflux.service.ChatMessageService;
import com.example.chat_webflux.service.SubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class JsonMessageRouter {

    private final ChatMessageService chatMessageService;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    public void handleJsonMessage(Sinks.Many<String> sessionSink, WebSocketSession session, String jsonMessage) {
        try {
            WsJsonMessage wsJsonMessage = objectMapper.readValue(jsonMessage, WsJsonMessage.class);
            switch (wsJsonMessage.getType()) {
                case "SEND":
                    handleSend(jsonMessage);
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

    private void handleSend(String jsonMessage) throws JsonProcessingException {
        WsJsonMessage<SendMessageInfo> wsJsonMessage = objectMapper.readValue(
                jsonMessage,
                new TypeReference<WsJsonMessage<SendMessageInfo>>() {}
        );

        chatMessageService.sendChatMessageKafkaEvent(new ChatMessage(wsJsonMessage.getData()), false)
                .subscribe();
    }

    private void handleSubscribe(Sinks.Many<String> sessionSink, WebSocketSession session, WsJsonMessage wsJsonMessage) {
        String sessionId = session.getId();
        String destination = wsJsonMessage.getDestination();

        // 채팅방 메시지 구독
        if (destination.startsWith("/topic/message/")) {
            subscriptionService.subscribeRoomMessage(sessionSink, destination, sessionId);
        }
        // 채팅방 생성 구독
        else if (destination.startsWith("/topic/rooms")) {
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
