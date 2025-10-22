package com.example.chat_webflux.kafka;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Kafka 이벤트로 사용될 객체에 구현해주어야 한다.
 * 직렬화 시 @class 필드를 추가시켜주는 설정
 * {
 *   "@class": "com.example.ChatMessage",
 *   "sender": "tester",
 *   "roomId": 1,
 *   "content": "Hello"
 * }
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public interface KafkaEvent {

}
