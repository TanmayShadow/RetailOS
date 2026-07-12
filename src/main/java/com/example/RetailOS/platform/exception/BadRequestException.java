package com.example.RetailOS.platform.exception;

public class BadRequestException extends ApiException{
    public BadRequestException(String message) {
        super(message);
    }
}
