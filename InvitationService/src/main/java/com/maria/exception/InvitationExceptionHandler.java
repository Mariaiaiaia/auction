package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class InvitationExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericExceptions(Exception e) {
        return Mono.just(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DatabaseOperationException.class)
    public Mono<Map<String, String>> handleDatabaseOperationException(DatabaseOperationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InvitationNotExistException.class)
    public Mono<Map<String, String>> handleInvitationNotExistException(InvitationNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidAcceptanceRequestException.class)
    public Mono<Map<String, String>> handleInvalidAcceptanceRequestException(InvalidAcceptanceRequestException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }
}
