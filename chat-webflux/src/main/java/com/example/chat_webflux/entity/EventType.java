package com.example.chat_webflux.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    USER_CREATED("user.created"),
    CHAT_ROOM_CREATED("chatRoom.created");

    private final String value;
}
