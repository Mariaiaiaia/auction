package com.maria.exception;

public class AuctionNotExistException extends RuntimeException {
    public AuctionNotExistException(String message) {
        super(message);
    }
}
