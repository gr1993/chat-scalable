package org.loadtester;

import org.loadtester.config.ConfigLoader;
import org.loadtester.dto.LoadTestConfig;
import org.loadtester.service.WebSocketClient;

public class Main {
    private final static LoadTestConfig config = ConfigLoader.load("config.json");

    public static void main(String[] args) {
        WebSocketClient webSocketClient = new WebSocketClient(config.getWebSocketEndpoint());
        webSocketClient.connect();

        try {
            Thread.sleep(10000);
            String test = webSocketClient.getSessionId();
            String test2 = "";

        } catch (Exception ex) {

        }
    }
}