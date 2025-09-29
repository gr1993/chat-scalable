package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 입장 처리
     */
    public Mono<Void> enterUser(String userId) {
        return userRepository.existsById(userId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("이미 존재하는 아이디입니다."));
                    }
                    return userRepository.save(new ChatUser(userId)).then();
                });
    }
}
