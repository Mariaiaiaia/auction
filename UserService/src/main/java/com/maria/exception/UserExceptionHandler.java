package com.maria.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> handleUserNotExistExceptions(UserNotExistException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(UserNotAvailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<Map<String, String>> handleUserNotAvailableExceptions(UserNotAvailableException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleUserAlreadyExistsExceptions(UserAlreadyExistsException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DatabaseOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleDatabaseOperationExceptions(DatabaseOperationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleValidationExceptions(ConstraintViolationException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }


    @ExceptionHandler({InvalidEmailException.class, InvalidUserIdException.class,
            InvalidUserRegistrationDTOException.class, InvalidUserUpdateDTOException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleValidationExceptions(RuntimeException e) {
        return Mono.just(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericExceptions(Exception e) {
        return Mono.just(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
    }
}
