package com.example.chat_webflux.controller;

import com.example.chat_webflux.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;


    @Test
    void enterUser_성공() throws Exception {
        //given
        String userId = "abc";
        when(userService.enterUser(userId)).thenReturn(Mono.empty());

        //when & then
        webTestClient.post()
                .uri("/api/user/enter/" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("요청 성공")
                .jsonPath("$.data").doesNotExist();

        verify(userService).enterUser(userId);
    }

    @Test
    void enterUser_실패_아이디중복() throws Exception {
        //given
        String userId = "abc";
        String errorMsg = "이미 존재하는 아이디입니다.";
        doThrow(new IllegalArgumentException(errorMsg))
                .when(userService).enterUser(any());

        //when & then
        webTestClient.post()
                .uri("/api/user/enter/" + userId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo(errorMsg)
                .jsonPath("$.data").doesNotExist();
    }

}