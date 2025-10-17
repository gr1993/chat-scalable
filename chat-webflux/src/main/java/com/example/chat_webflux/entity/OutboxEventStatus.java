package com.example.chat_webflux.entity;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED;
}
