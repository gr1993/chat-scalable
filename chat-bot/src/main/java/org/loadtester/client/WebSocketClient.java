package org.loadtester.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Getter
public class WebSocketClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String url;
    private WebSocket webSocket;
    private String sessionId;
    private boolean connected = false;

    public WebSocketClient(String url) {
        this.url = url;
    }

    public void connect() {
        HttpClient client = HttpClient.newHttpClient();
        webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        connected = true;
                        System.out.println("웹소켓 연결 성공");
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        System.out.println("메시지 수신: " + data);
                        try {
                            Map<String, Object> map = mapper.readValue(String.valueOf(data), Map.class);
                            if ("session-info".equals(map.get("type"))) {
                                sessionId = (String)map.get("sessionId");
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        connected = false;
                        System.out.println("웹소켓 세션이 종료되었습니다. : " + reason);
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        connected = false;
                        System.err.println("예외 발생: " + error.getMessage());
                    }
                }).join();
    }

    public void send() {
        webSocket.sendText("Ping from client ", true);
    }
}
