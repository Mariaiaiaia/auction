package com.maria.exception;

public class InvalidUserIdException extends RuntimeException{
    public InvalidUserIdException(String message){
        super(message);
    }
}
