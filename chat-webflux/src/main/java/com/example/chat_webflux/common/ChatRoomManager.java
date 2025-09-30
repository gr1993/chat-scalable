package com.example.chat_webflux.common;

import lombok.Getter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomManager {
    // 채팅서버 구독자 -> 채팅방 생성 알림 메시지 스트림
    @Getter
    private final Sinks.Many<String> chatServerSinks = Sinks.many().multicast().onBackpressureBuffer();

    // 채팅방 ID -> 해당 채팅방의 메시지 스트림
    private final ConcurrentHashMap<String, Sinks.Many<String>> roomSinks = new ConcurrentHashMap<>();

    public Sinks.Many<String> getRoomSink(String roomId) {
        return roomSinks.computeIfAbsent(roomId, k ->
                Sinks.many().multicast().onBackpressureBuffer()
        );
    }
}
