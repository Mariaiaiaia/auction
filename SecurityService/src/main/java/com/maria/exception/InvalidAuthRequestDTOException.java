package com.maria.exception;

public class InvalidAuthRequestDTOException extends RuntimeException {
    public InvalidAuthRequestDTOException(String message) {
        super(message);
    }
}
