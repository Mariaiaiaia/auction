package com.maria.exception;

public class AuctionStatusException extends RuntimeException{
    public AuctionStatusException(String message){
        super(message);
    }
}
