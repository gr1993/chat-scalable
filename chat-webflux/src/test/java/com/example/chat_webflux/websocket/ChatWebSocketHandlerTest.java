package com.example.chat_webflux.websocket;

import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.service.ChatRoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 웹소켓 핸들러 통합 테스트 클래스
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChatWebSocketHandlerTest {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private ReactorNettyWebSocketClient client;
    private URI uri;

    @BeforeEach
    public void setup() {
        client = new ReactorNettyWebSocketClient();
        uri = URI.create("ws://localhost:" + port + "/ws");
    }

    /**
     * 채팅방 생성 구독 통합 테스트
     */
    @Test
    void createRoom_성공() throws Exception {
        // given
        String roomName = "park";
        BlockingQueue<WsJsonMessage<ChatRoomInfo>> blockingQueue = new LinkedBlockingQueue<>();

        // when & then
        getSubscriptionTest(
            "/topic/rooms",
            new TypeReference<>() {},
            blockingQueue,
            session -> chatRoomService.createRoom("park")
                .then(Mono.fromCallable(() -> {
                    WsJsonMessage<ChatRoomInfo> wsMsg = blockingQueue.poll(5, TimeUnit.SECONDS);
                    assertNotNull(wsMsg);
                    assertEquals("ROOM_CREATED", wsMsg.getType());
                    assertEquals("/topic/rooms", wsMsg.getDestination());
                    assertNotNull(wsMsg.getData());

                    ChatRoomInfo chatRoomInfo = wsMsg.getData();
                    log.info("받은 메세지 객체 : {}", chatRoomInfo);
                    assertNotNull(chatRoomInfo.getRoomId());
                    assertEquals(roomName, chatRoomInfo.getRoomName());
                    return wsMsg;
                }))
                .then() // 업스트림의 Mono<T> 반환 값을 무시하고 Mono<Void>로 변환
        ).block();
    }

    private <T> Mono<Void> getSubscriptionTest(
            String destination,
            TypeReference<WsJsonMessage<T>> typeReference,
            BlockingQueue<WsJsonMessage<T>> blockingQueue,
            Function<WebSocketSession, Mono<Void>> serviceLogic
    ) {
        return client.execute(uri, session -> {

            // 수신
            Mono<Void> inputReceive = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .flatMap(json -> {
                        try {
                            WsJsonMessage<T> obj = objectMapper.readValue(json, typeReference);
                            return Mono.just(obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Mono.empty();
                        }
                    })
                    .doOnNext(blockingQueue::offer)
                    .then();

            // 송신(구독 요청)
            String jsonStr = "";
            try {
                jsonStr = objectMapper.writeValueAsString(new WsJsonMessage("SUBSCRIBE", destination));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            Mono<Void> outputSend = session.send(Mono.just(session.textMessage(jsonStr))).then();

            Mono<Void> logicAndClose = outputSend
                    .then(Mono.delay(Duration.ofMillis(200)))
                    .then(serviceLogic.apply(session))
                    .then(session.close());

            // 수신과 송신/로직을 경쟁(race) 또는 합치기
            return Mono.when(inputReceive, logicAndClose)
                    .then();
        });
    }

}
