package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.OutboxEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, UUID> {
    Flux<OutboxEvent> findByStatus(String status);

    @Query("UPDATE outbox_event SET status = :status WHERE event_id = :id")
    Mono<Integer> updateStatus(UUID id, String status);
}
