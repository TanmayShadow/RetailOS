package com.example.RetailOS.platform.common;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;

    private final String message;

    private final T data;

    @Builder.Default
    private final Instant timestamp = Instant.now();
}
