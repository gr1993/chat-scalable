package org.loadtester;

import org.loadtester.config.ConfigLoader;
import org.loadtester.dto.LoadTestConfig;
import org.loadtester.service.ChatService;

public class Main {
    private final static LoadTestConfig config = ConfigLoader.load("config.json");
    private final static ChatService chatService = new ChatService(config.getRestApiBaseUrl());

    public static void main(String[] args) {
        Long roomId = chatService.getExistRoomId();
    }
}