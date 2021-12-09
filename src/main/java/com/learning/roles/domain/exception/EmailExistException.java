package com.learning.roles.domain.exception;

public class EmailExistException extends Exception{

    public EmailExistException(String message){
        super(message);
    }
}
