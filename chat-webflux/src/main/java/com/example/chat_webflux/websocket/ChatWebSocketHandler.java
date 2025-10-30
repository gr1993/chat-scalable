package com.example.chat_webflux.websocket;

import com.example.chat_webflux.common.ChatSessionManager;
import com.example.chat_webflux.common.RoomUserSessionManager;
import com.example.chat_webflux.dto.WebSocketRoomUser;
import com.example.chat_webflux.service.ChatRoomService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatRoomService chatRoomService;
    private final JsonMessageRouter jsonMessageRouter;
    private final ChatSessionManager chatSessionManager;
    private final RoomUserSessionManager roomUserSessionManager;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    @PostConstruct
    public void initMetrics() {
        meterRegistry.gauge("chat_app_active_connections", activeConnections);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String sessionId = session.getId();
        Sinks.Many<String> sessionSink = chatSessionManager.createSessionSink(sessionId);

        // 연결 직후 클라이언트에게 sessionId 전송
        sessionSink.tryEmitNext(
                "{\"type\": \"session-info\", \"sessionId\": \"" + sessionId + "\"}"
        );

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> jsonMessageRouter.handleJsonMessage(sessionSink, session, message))
                .doFinally(signal -> {
                    // 커넥션 종료 시 로직
                    chatSessionManager.completeSessionSink(sessionId);
                    WebSocketRoomUser roomUser = roomUserSessionManager.removeUserSession(sessionId);
                    if (roomUser != null) {
                        // 해당 사용자 퇴장 처리
                        chatRoomService.exitRoom(roomUser.getRoomId(), roomUser.getUserId());
                    }
                    activeConnections.decrementAndGet();
                })
                .then();

        Flux<WebSocketMessage> output = sessionSink.asFlux()
                .map(session::textMessage);

        // 웹소켓 연결 커스텀 지표 수집
        activeConnections.incrementAndGet();

        return session.send(output).and(input);
    }
}
