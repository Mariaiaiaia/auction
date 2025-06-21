package com.maria.exception;

public class ItemNotExistException extends RuntimeException {
    public ItemNotExistException(String message) {
        super(message);
    }
}
