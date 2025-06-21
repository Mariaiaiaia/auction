package com.maria.exception;

public class InvitationNotExistException extends RuntimeException {
    public InvitationNotExistException(String message) {
        super(message);
    }
}
