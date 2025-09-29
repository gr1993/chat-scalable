package com.example.chat_webflux.controller;

import com.example.chat_webflux.dto.ApiResponse;
import com.example.chat_webflux.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/enter/{userId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> enterUser(@PathVariable String userId) {
        return userService.enterUser(userId)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.ok())));
    }
}
