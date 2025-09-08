package com.example.chat_webflux.common;

import com.example.chat_webflux.dto.WebSocketRoomUser;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RoomUserSessionManager {
    private final ConcurrentMap<String, WebSocketRoomUser> sessionIdToRoomUser = new ConcurrentHashMap<>();

    public void addRoomUserSession(String sessionId, WebSocketRoomUser roomUser) {
        if (sessionId != null && roomUser != null) {
            sessionIdToRoomUser.put(sessionId, roomUser);
        }
    }

    public WebSocketRoomUser getRoomUser(String sessionId) {
        return sessionIdToRoomUser.get(sessionId);
    }

    public WebSocketRoomUser removeUserSession(String sessionId) {
        return sessionIdToRoomUser.remove(sessionId);
    }
}