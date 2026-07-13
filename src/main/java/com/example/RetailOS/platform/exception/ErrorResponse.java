package com.example.RetailOS.platform.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private final boolean success;

    private final String message;

    private final int status;

    private final String path;

    private final Instant timestamp;

    private List<ValidationError> errors;
}
