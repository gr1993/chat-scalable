package com.example.chat_webflux.websocket;

import com.example.chat_webflux.common.TriFunction;
import com.example.chat_webflux.dto.ChatMessageInfo;
import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.dto.SendMessageInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.entity.MessageType;
import com.example.chat_webflux.integration.EmbeddedRedisExtension;
import com.example.chat_webflux.kafka.handler.ChatRoomNotificationHandler;
import com.example.chat_webflux.repository.ChatMessageRepository;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.example.chat_webflux.repository.UserRepository;
import com.example.chat_webflux.service.ChatRoomService;
import com.example.chat_webflux.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 웹소켓 핸들러 통합 테스트 클래스
 */
@Slf4j
@ExtendWith(EmbeddedRedisExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChatWebSocketHandlerTest {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatRoomNotificationHandler chatRoomNotificationHandler;

    @LocalServerPort
    private int port;

    private ReactorNettyWebSocketClient client;
    private URI uri;


    @BeforeEach
    public void setup() {
        client = new ReactorNettyWebSocketClient();
        uri = URI.create("ws://localhost:" + port + "/ws");
    }

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll().block();
        chatRoomRepository.deleteAll().block();
    }


    /**
     * 메세지 전송 및 구독까지 통합 테스트
     */
    @Test
    void sendMessage_성공() throws Exception {
        getMsgSubsciptionTest(true, (session, roomId, userId) -> {
            String message = "안녕하세요~";
            SendMessageInfo messageInfo = new SendMessageInfo(
                    roomId,
                    userId,
                    message
            );

            String jsonStr = getJsonStr("SEND", "/api/messages", messageInfo);
            Mono<Void> outputSend = session.send(Mono.just(session.textMessage(jsonStr)))
                    .then();
            outputSend.subscribe();
            return Mono.just(message);
        });
    }

    /**
     * 채팅방 생성 구독 통합 테스트
     */
    @Test
    void createRoom_성공() throws Exception {
        // given
        Long roomId = 1L;
        String roomName = "park";
        BlockingQueue<WsJsonMessage<ChatRoomInfo>> blockingQueue = new LinkedBlockingQueue<>();

        // when & then
        getSubscriptionTest(
            "/topic/rooms",
            new TypeReference<>() {},
            blockingQueue,
            session -> chatRoomNotificationHandler.handle(new ChatRoom(roomId, roomName))
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

    /**
     * 채팅방 입장 메시지 구독 통합 테스트
     */
    @Test
    void enterRoom_성공() throws Exception {
        getMsgSubsciptionTest(
                false,
                (session, roomId, userId) -> chatRoomService.enterRoom(roomId, userId).thenReturn("")
        );
    }

    /**
     * 채팅방 퇴장 메시지 구독 통합 테스트
     */
    @Test
    void exitRoom_성공() throws Exception {
        getMsgSubsciptionTest(
                false,
                (session, roomId, userId) -> chatRoomService.exitRoom(roomId, userId).thenReturn("")
        );
    }


    private void getMsgSubsciptionTest(boolean isUserMsg, TriFunction<WebSocketSession, Long, String, Mono<String>> function) {
        // given
        String roomName = "park";
        chatRoomService.createRoom(roomName).block();
        List<ChatRoomInfo> roomInfoList = chatRoomService.getRoomList().block();

        Long roomId = roomInfoList.get(0).getRoomId();
        String userId = "lim";
        if (Boolean.FALSE.equals(userRepository.existsById(userId).block())) {
            userService.enterUser(userId).block();
        }

        BlockingQueue<WsJsonMessage<ChatMessageInfo>> blockingQueue = new LinkedBlockingQueue<>();

        // when & then
        getSubscriptionTest(
                "/topic/message/" + roomId,
                new TypeReference<>() {},
                blockingQueue,
                session -> function.apply(session, roomId, userId)
                        .flatMap(message -> Mono.fromCallable(() -> {
                            WsJsonMessage<ChatMessageInfo> wsMsg = blockingQueue.poll(5, TimeUnit.SECONDS);
                            assertNotNull(wsMsg);
                            assertEquals("ROOM_MESSAGE", wsMsg.getType());
                            assertEquals("/topic/message/" + roomId, wsMsg.getDestination());
                            assertNotNull(wsMsg.getData());

                            ChatMessageInfo chatMessageInfo = wsMsg.getData();
                            log.info("받은 메세지 객체 : {}", chatMessageInfo);
                            assertNotNull(chatMessageInfo);
                            assertNotNull(chatMessageInfo.getMessageId());
                            assertEquals(userId, chatMessageInfo.getSenderId());
                            assertTrue(StringUtils.hasText(chatMessageInfo.getMessage()));
                            if (StringUtils.hasText(message)) {
                                assertEquals(message, chatMessageInfo.getMessage());
                            }
                            assertTrue(StringUtils.hasText(chatMessageInfo.getSendDt()));
                            assertEquals((isUserMsg ? MessageType.user.name() : MessageType.system.name()), chatMessageInfo.getType());
                            return wsMsg;
                        }))
                        .then()
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
            String jsonStr = getJsonStr("SUBSCRIBE", destination);
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

    private String getJsonStr(String type, String destination) {
        return getJsonStr(type, destination, null);
    }

    private String getJsonStr(String type, String destination, Object data) {
        String jsonStr = "";
        WsJsonMessage wsJsonMessage;
        try {
            if (data == null) {
                wsJsonMessage = new WsJsonMessage(type, destination);
            } else {
                wsJsonMessage = new WsJsonMessage(type, destination, data);
            }

            jsonStr = objectMapper.writeValueAsString(wsJsonMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonStr;
    }

}
