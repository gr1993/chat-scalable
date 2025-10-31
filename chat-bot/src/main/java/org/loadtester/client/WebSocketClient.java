package org.loadtester.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.loadtester.dto.WsJsonMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Getter
public class WebSocketClient {

    private final String url;
    private WebSocket webSocket;
    private String sessionId;
    private boolean connected = false;
    private final StringBuilder messageBuffer = new StringBuilder();
    private final ObjectMapper mapper = new ObjectMapper();

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
                        messageBuffer.append(data);

                        if (last) {
                            String completeMessage = messageBuffer.toString();
                            messageBuffer.setLength(0);
                            System.out.println("메시지 수신: " + completeMessage);

                            try {
                                Map<String, Object> map = mapper.readValue(String.valueOf(completeMessage), Map.class);
                                if ("session-info".equals(map.get("type"))) {
                                    sessionId = (String)map.get("sessionId");
                                }
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        webSocket.request(1);
                        return CompletableFuture.completedFuture(null);
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

    public void disconnect() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Bye")
                .thenRun(() -> System.out.println("웹소켓 세션이 종료되었습니다."));
    }

    public <T> void send(T data) throws JsonProcessingException {
        WsJsonMessage<T> wsJsonMessage = new WsJsonMessage<>(
                "SEND",
            "/api/messages",
                data
        );
        String json = mapper.writeValueAsString(wsJsonMessage);
        webSocket.sendText(json, true);
    }

    public void subscribe(String destination) throws JsonProcessingException {
        WsJsonMessage wsJsonMessage = new WsJsonMessage(
                "SUBSCRIBE",
                destination
        );
        String json = mapper.writeValueAsString(wsJsonMessage);
        webSocket.sendText(json, true);
    }
}
