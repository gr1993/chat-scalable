package com.example.chat_webflux.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatKafkaProducerPool {

    private final BlockingQueue<ReactiveKafkaProducerTemplate<String, Object>> pool = new LinkedBlockingQueue<>();

    private final KafkaProperties kafkaProperties;
    private final String serverId;

    public ChatKafkaProducerPool(KafkaProperties props, String serverId, int poolSize) {
        this.kafkaProperties = props;
        this.serverId = serverId;

        for (int i = 0; i < poolSize; i++) {
            pool.add(createNewProducer(i));
        }
    }

    public Mono<Void> sendKafkaEvent(List<SenderRecord<String, Object, Object>> recordList) {
        return this.acquire()
            .flatMap(producer ->
                producer.sendTransactionally(
                    Flux.fromIterable(recordList)
                )
                .then()
                .doFinally(signal -> this.release(producer))
            )
            .then();
    }

    private ReactiveKafkaProducerTemplate<String, Object> createNewProducer(int idx) {
        Map<String, Object> producerProps = new HashMap<>(kafkaProperties.buildProducerProperties());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-" + serverId + "-" + idx);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(producerProps));
    }

    private Mono<ReactiveKafkaProducerTemplate<String, Object>> acquire() {
        return Mono.fromCallable(() -> pool.take());
    }

    private void release(ReactiveKafkaProducerTemplate<String, Object> producer) {
        pool.offer(producer);
    }
}
