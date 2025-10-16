package com.example.chat_webflux.controller;

import com.example.chat_webflux.common.RoomUserSessionManager;
import com.example.chat_webflux.dto.WebSocketRoomUser;
import com.example.chat_webflux.service.ChatRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ChatRoomRestController.class)
public class ChatRoomRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private RoomUserSessionManager roomUserSessionManager;


    @Test
    void getRoomList_성공() throws Exception {
        //given
        when(chatRoomService.getRoomList()).thenReturn(Mono.just(new ArrayList<>()));

        //when & then
        webTestClient.get()
                .uri("/api/room")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("요청 성공")
                .jsonPath("$.data").isArray();

        verify(chatRoomService).getRoomList();
    }

    @Test
    void createRoom_성공() throws Exception {
        //given
        String name = "park";
        when(chatRoomService.createRoom(any(String.class))).thenReturn(Mono.empty());

        //when & then
        webTestClient.post()
                .uri("/api/room")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", name))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("요청 성공")
                .jsonPath("$.data").doesNotExist();

        verify(chatRoomService).createRoom(name);
    }

    @Test
    void enterRoom_성공() throws Exception {
        //given
        Long roomId = 1L;
        String userId = "park";
        String sessionId = "ABCDEFG";

        when(chatRoomService.enterRoom(any(Long.class), any(String.class))).thenReturn(Mono.empty());

        //when & then
        webTestClient.post()
                .uri("/api/room/" + roomId + "/enter")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-Session-Id", sessionId)
                .body(BodyInserters.fromFormData("userId", userId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("요청 성공")
                .jsonPath("$.data").doesNotExist();

        verify(roomUserSessionManager).addRoomUserSession(sessionId, new WebSocketRoomUser(roomId, userId));
        verify(chatRoomService).enterRoom(roomId, userId);
    }

    @Test
    void exitRoom_성공() throws Exception {
        //given
        Long roomId = 1L;
        String userId = "park";

        when(chatRoomService.exitRoom(any(Long.class), any(String.class))).thenReturn(Mono.empty());

        //when & then
        webTestClient.post()
                .uri("/api/room/" + roomId + "/exit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("userId", userId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("요청 성공")
                .jsonPath("$.data").doesNotExist();

        verify(chatRoomService).exitRoom(roomId, userId);
    }
}
