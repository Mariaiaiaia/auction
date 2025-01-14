package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class AuctionExceptionHandler {

    @ExceptionHandler(ItemNotExistException.class)
    public Mono<ServerResponse> handleItemNotExistException(ItemNotExistException e){
        return ServerResponse
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }


    @ExceptionHandler(DataForAuctionIsNotValid.class)
    public Mono<ServerResponse> handleTimeIncorrectException(DataForAuctionIsNotValid e){
        return ServerResponse
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(AuctionNotExistException.class)
    public Mono<ServerResponse> handleAuctionNotExistException(AuctionNotExistException e){
        return ServerResponse
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(UserException.class)
    public Mono<ServerResponse> handleUserNotExistException(UserException e){
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public Mono<ServerResponse> handleDatabaseOperationException(DatabaseOperationException e){
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(AuctionNotAvailableException.class)
    public Mono<ServerResponse> handleAuctionNotAvailableException(AuctionNotAvailableException e){
        return ServerResponse
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(BitNotPossibleException.class)
    public Mono<ServerResponse> handleBitNotPossibleException(BitNotPossibleException e){
        return ServerResponse
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(AuctionWebClientException.class)
    public Mono<ServerResponse> handleAuctionWebClientException (AuctionWebClientException e){
        return ServerResponse
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(Exception.class)
    public Mono<ServerResponse> handleGeneralException(Exception e){
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"An unexpected error occurred.\"}");
    }

    @ExceptionHandler(RedisOperationException.class)
    public Mono<ServerResponse> handleRedisOperationException(RedisOperationException e){
        return ServerResponse
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(AuctionStatusException.class)
    public Mono<ServerResponse> handleAuctionStatusException(AuctionStatusException e){
        return ServerResponse
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }
}
