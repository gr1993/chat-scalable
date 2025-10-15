package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.ChatMessage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {

}
