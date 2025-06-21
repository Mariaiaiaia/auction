package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class SecurityExceptionHandler {
    @ExceptionHandler(PasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<Map<String, String>> handlePasswordException(PasswordException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidAuthRequestDTOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleInvalidAuthRequestDTOException(InvalidAuthRequestDTOException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericExceptions(Exception e) {
        return Mono.just(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
    }
}
