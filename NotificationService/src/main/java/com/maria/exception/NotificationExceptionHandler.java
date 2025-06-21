package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class NotificationExceptionHandler {
    @ExceptionHandler(NotificationNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> handleNotificationNotExistException(NotificationNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DatabaseOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleDatabaseOperationException(DatabaseOperationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(NotificationNotAvailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<Map<String, String>> handleNotificationNotAvailableException(NotificationNotAvailableException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> handleAuctionNotExistException(AuctionNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(NotificationWebClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> handleNotificationWebClientException(NotificationWebClientException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidNotificationIdException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleInvalidNotificationIdException(InvalidNotificationIdException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericExceptions(Exception e) {
        return Mono.just(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
    }
}
