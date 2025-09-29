package com.example.chat_webflux.exception;

import com.example.chat_webflux.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleException(Exception ex) {
        return Mono.just(buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Mono.just(buildResponseEntity(HttpStatus.BAD_REQUEST, ex));
    }

    private ResponseEntity<ApiResponse<Void>> buildResponseEntity(HttpStatus status, Exception ex) {
        String message = ex.getMessage();
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            message = cause.getMessage();
        }

        log.error("API Error: ", ex);
        return ResponseEntity.status(status).body(ApiResponse.fail(message));
    }
}
