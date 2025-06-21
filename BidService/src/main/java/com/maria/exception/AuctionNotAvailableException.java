package com.maria.exception;

public class AuctionNotAvailableException extends RuntimeException {
    public AuctionNotAvailableException(String message) {
        super(message);
    }
}
