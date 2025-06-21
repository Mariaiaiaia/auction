package com.maria.exception;

public class BidExistsException extends RuntimeException{
    public BidExistsException(String message){
        super(message);
    }
}
