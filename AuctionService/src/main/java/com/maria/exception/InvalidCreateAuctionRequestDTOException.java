package com.maria.exception;

public class InvalidCreateAuctionRequestDTOException extends RuntimeException{
    public InvalidCreateAuctionRequestDTOException(String message){
        super(message);
    }
}
