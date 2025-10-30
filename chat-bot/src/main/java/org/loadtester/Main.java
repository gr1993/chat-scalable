package org.loadtester;

import org.loadtester.client.WebSocketClient;
import org.loadtester.config.ConfigLoader;
import org.loadtester.dto.LoadTestConfig;
import org.loadtester.service.ChatService;
import org.loadtester.util.MessageUtil;

public class Main {
    private final static LoadTestConfig config = ConfigLoader.load("config.json");
    private final static ChatService chatService = new ChatService(config.getRestApiBaseUrl());

    private final static String SEND_MESSAGE = MessageUtil.generateRandomMessage(config.getMessageLength());
    private static Long roomId = 0L;

    public static void main(String[] args) {
        try {
            roomId = chatService.getExistRoomId();

            int userCount = config.getUserCount();
            int rampUpTimeSeconds = config.getRampUpTimeSeconds();
            long delayMillis = (rampUpTimeSeconds * 1000L) / userCount;

            for (int i = 1; i < userCount + 1; i++) {
                int userId = i;
                Thread userThread = new Thread(() -> {
                    simulateUser(config.getUserIdPrefix() + userId);
                });
                userThread.start();

                // 각 사용자 생성 사이에 delay 추가
                Thread.sleep(delayMillis);
            }
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

            // 채팅방 입장 API
            chatService.enterRoom(roomId, userId, sessionId);

            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}