package com.maria.exception;

public class InvalidUserRegistrationDTOException extends RuntimeException{
    public InvalidUserRegistrationDTOException(String message){
        super(message);
    }
}
