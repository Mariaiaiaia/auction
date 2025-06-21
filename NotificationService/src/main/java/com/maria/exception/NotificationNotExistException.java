package com.maria.exception;

public class NotificationNotExistException extends RuntimeException {
    public NotificationNotExistException(String message) {
        super(message);
    }
}
