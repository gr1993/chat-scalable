package com.example.chat_webflux.service;

import com.example.chat_webflux.dto.WsJsonMessage;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber {

    private final ReactiveRedisConnectionFactory connectionFactory;
    private final Jackson2JsonRedisSerializer<Object> jsonSerializer;
    private final String TOPIC_NAME = "chat-channel";
    @Getter
    private Flux<WsJsonMessage> inboundMessageFlux;

    @PostConstruct
    public void subscribe() {
        inboundMessageFlux = subscribeToMessages();
        inboundMessageFlux.subscribe();
    }

    public Flux<WsJsonMessage> subscribeToMessages() {
        ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(connectionFactory);

        return container.receive(ChannelTopic.of(TOPIC_NAME))
                .flatMap(message -> Mono.justOrEmpty(jsonSerializer.deserialize(message.getMessage().getBytes())))
                .doOnError(throwable -> System.err.println("Error occurred: " + throwable.getMessage()))
                .cast(WsJsonMessage.class);
    }
}
