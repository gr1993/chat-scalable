package org.loadtester;

import org.loadtester.client.WebSocketClient;
import org.loadtester.config.ConfigLoader;
import org.loadtester.dto.LoadTestConfig;
import org.loadtester.dto.SendMessageInfo;
import org.loadtester.service.ChatService;
import org.loadtester.util.FileLogger;
import org.loadtester.util.MessageUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    private final static LoadTestConfig config = ConfigLoader.load("config.json");
    private final static ChatService chatService = new ChatService(config.getRestApiBaseUrl());

    private final static String SEND_MESSAGE = MessageUtil.generateRandomMessage(config.getMessageLength());
    private static Long roomId = 0L;

    private final static AtomicLong totalMessageCount = new AtomicLong();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static CountDownLatch userCompletionLatch;

    public static void main(String[] args) {
        try {
            roomId = chatService.getExistRoomId();

            int userCount = config.getUserCount();
            int rampUpTimeSeconds = config.getRampUpTimeSeconds();
            long delayMillis = (rampUpTimeSeconds * 1000L) / userCount;
            userCompletionLatch = new CountDownLatch(userCount);

            for (int i = 1; i < userCount + 1; i++) {
                int userId = i;
                Thread userThread = new Thread(() -> {
                    simulateUser(config.getUserIdPrefix() + userId);
                });
                userThread.start();

                // 각 사용자 생성 사이에 delay 추가
                Thread.sleep(delayMillis);
            }

            userCompletionLatch.await();

            String nowDateTime = LocalDateTime.now().format(formatter);
            FileLogger.init("test-result.txt");
            FileLogger.log("테스트 완료 시각 : " + nowDateTime);
            FileLogger.log("모든 사용자 시뮬레이션이 완료되어 메인 함수를 종료합니다.");
            FileLogger.log("총 메세지 전송 갯수 : " + totalMessageCount.get() + "개");
            FileLogger.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void simulateUser(String userId) {
        try {
            chatService.login(userId);

            // 웹소켓 연결(connect 메서드 안에 구독 메시지 콜백도 정의됨)
            WebSocketClient webSocketClient = new WebSocketClient(config.getWebSocketEndpoint());
            webSocketClient.connect();

            Thread.sleep(100);

            String sessionId = webSocketClient.getSessionId();

            // 채팅방 생성 구독
            webSocketClient.subscribe("/topic/rooms");
            // 채팅방 메시지 구독
            webSocketClient.subscribe("/topic/message/" + roomId);

            // 채팅방 입장 API
            chatService.enterRoom(roomId, userId, sessionId);

            // 메시지 전송 시작
            new Thread(() -> simulateSendMessage(webSocketClient, userId)).start();

        } catch (Exception ex) {
            ex.printStackTrace();
            userCompletionLatch.countDown();
        }
    }

    private static void simulateSendMessage(WebSocketClient webSocketClient, String userId) {
        try {
            long messageSendInterval = 1000L; // 1초마다 메시지 전송
            long chatDurationMillis = config.getChatDurationSeconds() * 1000L;
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < chatDurationMillis && webSocketClient.isConnected()) {
                // 메시지 전송
                SendMessageInfo messageInfo = new SendMessageInfo(
                        roomId,
                        userId,
                        SEND_MESSAGE
                );
                webSocketClient.send(messageInfo);
                totalMessageCount.incrementAndGet();

                Thread.sleep(messageSendInterval);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                // 채팅방 퇴장 API 호출
                chatService.exitRoom(roomId, userId);
            } catch (Exception ignored) {}

            try {
                if (webSocketClient.isConnected()) {
                    webSocketClient.disconnect();
                    System.out.println("웹소켓 세션이 종료되었습니다.");
                }
            } catch (Exception ignored) {}

            userCompletionLatch.countDown();
        }
    }
}