package com.example.chat_webflux.kafka;

public class KafkaTopics {
    private KafkaTopics() {}
    // 로드밸런싱 구독용 토픽
    public static final String CHAT_USER_CREATED = "chat.user.created";
    public static final String CHAT_ROOM_CREATED = "chat.room.created"; // 채팅방 생성 이벤트
    public static final String CHAT_MESSAGE_CREATED = "chat.message.created"; // 메시지 생성 이벤트

    // 브로드캐스트 구독용 토픽
    public static final String CHAT_ROOM_NOTIFICATION = "chat.room.notification"; // 채팅방 생성 알림
    public static final String CHAT_MESSAGE_NOTIFICATION = "chat.message.notification"; // 메시지 생성 알림
}
