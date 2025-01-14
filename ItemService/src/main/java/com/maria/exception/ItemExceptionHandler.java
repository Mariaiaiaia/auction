package com.maria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ItemExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ServerResponse> handleGeneralException(Exception e){
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"An unexpected error occurred.\"}");
    }

    @ExceptionHandler(ItemNotExistException.class)
    public Mono<ServerResponse> handleItemNotExistException(ItemNotExistException e){
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
}
