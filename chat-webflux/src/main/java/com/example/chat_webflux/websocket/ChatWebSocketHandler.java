package com.example.chat_webflux.websocket;

import com.example.chat_webflux.common.ChatSessionManager;
import com.example.chat_webflux.common.RoomUserSessionManager;
import com.example.chat_webflux.dto.WebSocketRoomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final JsonMessageRouter jsonMessageRouter;
    private final ChatSessionManager chatSessionManager;
    private final RoomUserSessionManager roomUserSessionManager;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String sessionId = session.getId();
        Sinks.Many<String> sessionSink = chatSessionManager.createSessionSink(sessionId);

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> jsonMessageRouter.handleJsonMessage(sessionSink, session, message))
                .doFinally(signal -> {
                    // 커넥션 종료 시 로직
                    chatSessionManager.completeSessionSink(sessionId);
                    WebSocketRoomUser roomUser = roomUserSessionManager.removeUserSession(sessionId);
                    if (roomUser != null) {
                        // 해당 사용자 퇴장 처리
                        //chatRoomService.exitRoom(roomUser.getRoomId(), roomUser.getUserId());
                    }
                })
                .then();

        Flux<WebSocketMessage> output = sessionSink.asFlux()
                .map(session::textMessage);

        return session.send(output).and(input);
    }
}
