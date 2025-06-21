package com.maria.exception;

public class InvalidPlaceBidRequestException extends RuntimeException {
    public InvalidPlaceBidRequestException(String message) {
        super(message);
    }
}
