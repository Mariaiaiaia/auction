package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ServerResponse> handleGeneralException(Exception e){
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"An unexpected error occurred.\"}");
    }

    @ExceptionHandler(NotificationNotExistException.class)
    public Mono<ServerResponse> handleNotificationNotExistException(NotificationNotExistException e){
        return ServerResponse
                .status(HttpStatus.NOT_FOUND)
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

    @ExceptionHandler(NotificationNotAvailableException.class)
    public Mono<ServerResponse> handleNotificationNotAvailableException(NotificationNotAvailableException e){
        return ServerResponse
                .status(HttpStatus.FORBIDDEN)
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

    @ExceptionHandler(NotificationWebClientException.class)
    public Mono<ServerResponse> handleNotificationWebClientException(NotificationWebClientException e){
        return ServerResponse
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

}
