package com.maria.exception;

public class InvalidNotificationIdException extends RuntimeException {
    public InvalidNotificationIdException(String message) {
        super(message);
    }
}
