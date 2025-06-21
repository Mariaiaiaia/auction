package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class BidExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericExceptions(Exception e) {
        return Mono.just(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
    }

    @ExceptionHandler(DataForBidNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleDataForBidNotValidException(DataForBidNotValidException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DatabaseOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleDatabaseOperationException(DatabaseOperationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> handleAuctionNotExistException(AuctionNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BidWebClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> handleBidWebClientException(BidWebClientException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuctionNotAvailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<Map<String, String>> handleAuctionNotAvailableException(AuctionNotAvailableException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({InvalidPlaceBidRequestException.class, InvalidIdException.class, BidExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleValidationException(RuntimeException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }
}
