package com.maria.exception;

public class InvalidUserUpdateDTOException extends RuntimeException{
    public InvalidUserUpdateDTOException(String message){
        super(message);
    }
}
