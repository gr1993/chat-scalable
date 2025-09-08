package com.example.chat_webflux.common;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {
    private final Map<String, Sinks.Many<String>> sessionSinks = new ConcurrentHashMap<>();

    public Sinks.Many<String> createSessionSink(String sessionId) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        sessionSinks.put(sessionId, sink);
        return sink;
    }

    public void completeSessionSink(String sessionId) {
        Optional.ofNullable(sessionSinks.remove(sessionId))
                .ifPresent(Sinks.Many::tryEmitComplete);
    }
}