package com.example.chat_webflux.websocket;

import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.service.ChatRoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 웹소켓 핸들러 통합 테스트 클래스
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
        BlockingQueue<ChatRoomInfo> blockingQueue = getSubscriptionQueue("/topic/rooms", ChatRoomInfo.class);

        // when
        String roomName = "park";
        chatRoomService.createRoom(roomName).block();

        // then
        ChatRoomInfo chatRoomInfo = blockingQueue.poll(5, TimeUnit.SECONDS);
        log.info("받은 메세지 객체 : {}", chatRoomInfo);
        assertNotNull(chatRoomInfo);
        assertEquals(roomName, chatRoomInfo.getRoomName());
    }

    private <T> BlockingQueue<T> getSubscriptionQueue(String destination, Class<T> clazz) {
        BlockingQueue<T> blockingQueue = new LinkedBlockingQueue<>();

        Mono<Void> sessionMono = client.execute(uri, session -> {

            // 수신
            Mono<Void> inputReceive = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .flatMap(json -> {
                        try {
                            T obj = objectMapper.readValue(json, clazz);
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

            // 수신 스트림을 백그라운드에서 실행
            inputReceive.subscribe();
            // 송신 후, 세션이 닫히지 않도록 무한정 대기
            return outputSend.then(Mono.never());
        });

        sessionMono.subscribe();
        return blockingQueue;
    }

}
