package com.example.chat_webflux.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    user("사용자"),
    system("시스템");

    private final String description;
}
