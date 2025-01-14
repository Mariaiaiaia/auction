package com.maria.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotExistException.class)
    public Mono<ServerResponse> handleUserNotExistException(UserNotExistException e){
        return ServerResponse
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(UserNotAvailableException.class)
    public Mono<ServerResponse> handleUserNotAvailableException(UserNotAvailableException e){
        return ServerResponse
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ServerResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e){
        return ServerResponse
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(e.getMessage());
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public Mono<ServerResponse> handleDatabaseOperationException(DatabaseOperationException e){
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"error\": \"" + e.getMessage() + "\"}");
    }
}
