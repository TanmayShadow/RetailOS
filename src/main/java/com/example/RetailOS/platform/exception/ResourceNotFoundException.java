package com.example.RetailOS.platform.exception;

public class ResourceNotFoundException extends ApiException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
