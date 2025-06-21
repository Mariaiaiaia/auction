package com.maria.exception;

public class InvalidAuctionUpdateDTOException extends RuntimeException{
    public InvalidAuctionUpdateDTOException(String message){
        super(message);
    }
}
