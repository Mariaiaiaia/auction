package com.maria.exception;

public class DataForAuctionIsNotValid extends RuntimeException{
    public DataForAuctionIsNotValid(String message){
        super(message);
    }
}
