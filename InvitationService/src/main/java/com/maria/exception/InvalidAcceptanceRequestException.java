package com.maria.exception;

public class InvalidAcceptanceRequestException extends RuntimeException {
    public InvalidAcceptanceRequestException(String message) {
        super(message);
    }
}
