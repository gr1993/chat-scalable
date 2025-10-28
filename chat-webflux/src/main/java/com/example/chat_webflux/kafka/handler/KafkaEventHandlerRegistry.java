package com.example.chat_webflux.kafka.handler;

import com.example.chat_webflux.kafka.KafkaEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KafkaEventHandlerRegistry {

    private final Map<String, KafkaEventHandler<?>> handlerMap = new HashMap<>();

    /**
     * Spring이 모든 @Component 핸들러를 주입해줌
     */
    public KafkaEventHandlerRegistry(List<KafkaEventHandler<?>> handlers) {
        for (KafkaEventHandler<?> handler : handlers) {
            handlerMap.put(handler.getTopic(), handler);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends KafkaEvent> KafkaEventHandler<T> getHandler(String topic) {
        return (KafkaEventHandler<T>) handlerMap.get(topic);
    }
}
