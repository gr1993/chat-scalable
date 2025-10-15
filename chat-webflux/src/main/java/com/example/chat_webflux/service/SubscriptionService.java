package com.example.chat_webflux.service;

import com.example.chat_webflux.common.ChatRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 웹소켓 구독용 서비스
 * 이 프로젝트에서는 구독키를 sessionId + destination 으로 지정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final ChatRoomManager chatRoomManager;

    private final ConcurrentHashMap<String, Disposable> subscriptions = new ConcurrentHashMap<>();

    /**
     * 채팅방 메시지 구독
     */
    public void subscribeRoomMessage(Sinks.Many<String> sessionSink,
                                     String destination,
                                     String sessionId) {
        String roomId = destination.substring("/topic/message/".length());

        // 해당 채팅방의 Sinks.Many를 가져오거나 없으면 새로 생성
        Sinks.Many<String> roomSink = chatRoomManager.getRoomSink(roomId);
        subscribe(roomSink, sessionSink, destination, sessionId);
    }

    /**
     * 채팅방 생성 구독
     */
    public void subscribeRoomCreate(Sinks.Many<String> sessionSink,
                                    String destination,
                                    String sessionId) {

        Sinks.Many<String> serverSinks = chatRoomManager.getChatServerSinks();
        subscribe(serverSinks, sessionSink, destination, sessionId);
    }

    /**
     * 구독 종료
     */
    public void unSubscribe(String sessionId, String destination) {
        String key = sessionId + ":" + destination;
        Disposable disposable = subscriptions.remove(key); // 맵에서 제거
        if (disposable != null) {
            disposable.dispose(); // 구독 취소
        }
    }

    private void subscribe(Sinks.Many<String> multiSink,
                           Sinks.Many<String> sessionSink,
                           String destination,
                           String sessionId) {

        // multiSink에서 발생한 데이터를 현재 세션(sessionSink)에 전달하도록 구독
        Disposable disposable = multiSink.asFlux()
                .subscribe(
                        // 이 메시지는 handle()의 output 스트림을 타고 클라이언트에게 전달
                        message -> sessionSink.tryEmitNext(message),
                        error -> {},
                        () -> {}
                );

        // 구독 취소를 위해 구독 정보 저장
        String key = sessionId + ":" + destination;
        subscriptions.put(key, disposable);
    }
}
