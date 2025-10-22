package com.example.chat_webflux.kafka;

public class KafkaTopics {
    private KafkaTopics() {}
    public static final String CHAT_USER_CREATED = "chat.user.created";
    public static final String CHAT_ROOM_CREATED = "chat.room.created";
    public static final String CHAT_MESSAGE_CREATED = "chat.message.created";
}
