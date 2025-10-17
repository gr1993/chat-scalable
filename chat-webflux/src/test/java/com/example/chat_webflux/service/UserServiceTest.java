package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.repository.OutboxEventRepository;
import com.example.chat_webflux.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;


    @Test
    void enterUser_성공() throws Exception {
        // given
        String userId = "abc";
        when(userRepository.existsById(userId)).thenReturn(Mono.just(false));
        when(userRepository.save(any(ChatUser.class))).thenReturn(Mono.just(new ChatUser(userId)));
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(Mono.just(new OutboxEvent(UUID.randomUUID())));
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("");

        // when
        userService.enterUser(userId).block();

        // then
        verify(userRepository).save(any(ChatUser.class));
    }

    @Test
    void enterUser_실패_아이디중복() {
        // given
        String userId = "abc";
        when(userRepository.existsById(userId)).thenReturn(Mono.just(true));

        // when & then
        StepVerifier.create(userService.enterUser(userId))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}