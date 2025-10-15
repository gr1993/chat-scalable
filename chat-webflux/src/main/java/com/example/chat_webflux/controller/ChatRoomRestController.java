package com.example.chat_webflux.controller;

import com.example.chat_webflux.common.RoomUserSessionManager;
import com.example.chat_webflux.dto.ApiResponse;
import com.example.chat_webflux.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class ChatRoomRestController {

    private final ChatRoomService chatRoomService;
    private final RoomUserSessionManager roomUserSessionManager;

    /**
     * WebFlux 환경에서는 x-www-form-urlencoded 일 때 @RequestParam 방식 말고 다른 방식의 body 파싱을 사용하여야 한다.
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> createRoom(ServerWebExchange exchange) {
        return extractParamValueFromFormData(exchange, "name")
                .flatMap(name -> {
                    return chatRoomService.createRoom(name)
                            .thenReturn(ResponseEntity.ok(ApiResponse.ok()));
                });
    }

    private Mono<String> extractParamValueFromFormData(ServerWebExchange exchange, String key) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String userId = formData.getFirst(key);
                    if (userId == null || userId.isBlank()) {
                        return Mono.error(new IllegalArgumentException(key + " 파라미터가 필요합니다."));
                    }
                    return Mono.just(userId);
                });
    }
}
