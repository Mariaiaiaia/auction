package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class AuctionExceptionHandler {
    @ExceptionHandler(ItemNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> handleItemNotExistException(ItemNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> handleAuctionNotExistException(AuctionNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleUserNotExistException(UserException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DatabaseOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleDatabaseOperationException(DatabaseOperationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionNotAvailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<Map<String, String>> handleAuctionNotAvailableException(AuctionNotAvailableException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BitNotPossibleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleBitNotPossibleException(BitNotPossibleException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionWebClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> handleAuctionWebClientException(AuctionWebClientException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericExceptions(Exception e) {
        return Mono.just(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
    }

    @ExceptionHandler(RedisOperationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> handleRedisOperationException(RedisOperationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleAuctionStatusException(AuctionStatusException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({InvalidIdException.class, InvalidAuctionUpdateDTOException.class,
    InvalidCreateAuctionRequestDTOException.class, DataForAuctionIsNotValid.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleValidationException(RuntimeException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }
}
