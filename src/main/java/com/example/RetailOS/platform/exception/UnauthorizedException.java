package com.example.RetailOS.platform.exception;

public class UnauthorizedException extends ApiException{
    public UnauthorizedException(String message) {
        super(message);
    }
}
