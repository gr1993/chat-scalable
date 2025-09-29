package com.example.chat_webflux.repository;

import com.example.chat_webflux.entity.ChatUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll().block();
    }

    @Test
    void save_findById_성공() {
        // given
        String userId = "abc1";
        ChatUser user = new ChatUser(userId);
        // 검증 시 불일치 문제로 createDt를 DB 정밀도(MICROS)에 맞춰 자름
        LocalDateTime expectedDt = user.getCreateDt().truncatedTo(ChronoUnit.MICROS);
        user.setCreateDt(expectedDt);

        // when
        userRepository.save(user).block();

        // then
        Mono<ChatUser> savedUser = userRepository.findById(userId);
        ChatUser expectedUser = new ChatUser();
        expectedUser.setId(userId);
        expectedUser.setNew(false);
        expectedUser.setCreateDt(expectedDt);

        StepVerifier.create(savedUser)
                .expectNext(expectedUser)
                .verifyComplete();
    }

    @Test
    void delete_성공() {
        // given
        String userId = "abc2";
        ChatUser user = new ChatUser(userId);
        userRepository.save(user).block();

        // when
        userRepository.deleteById(userId).block();

        // then
        Mono<ChatUser> savedUser = userRepository.findById(userId);
        StepVerifier.create(savedUser)
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    void findAll_성공() {
        // given
        userRepository.save(new ChatUser("abc")).block();
        userRepository.save(new ChatUser("def")).block();

        // when
        Flux<ChatUser> users = userRepository.findAll();

        // then
        StepVerifier.create(users)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

}