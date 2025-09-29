package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.ChatUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<ChatUser, String> {

}