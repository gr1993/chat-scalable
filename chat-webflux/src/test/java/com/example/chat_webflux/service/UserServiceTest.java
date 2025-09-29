package com.example.chat_webflux.service;

import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;


    @Test
    void enterUser_성공() {
        // given
        String userId = "abc";
        when(userRepository.existsById(userId)).thenReturn(Mono.just(false));
        when(userRepository.save(any(ChatUser.class))).thenReturn(Mono.just(new ChatUser(userId)));

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