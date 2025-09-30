package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.ChatRoom;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChatRoomRepository extends ReactiveCrudRepository<ChatRoom, Long> {

}
