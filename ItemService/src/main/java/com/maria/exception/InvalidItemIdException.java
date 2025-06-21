package com.maria.exception;

public class InvalidItemIdException extends RuntimeException {
    public InvalidItemIdException(String message) {
        super(message);
    }
}
