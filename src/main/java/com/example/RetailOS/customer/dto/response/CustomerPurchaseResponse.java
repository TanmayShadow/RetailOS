package com.example.RetailOS.customer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CustomerPurchaseResponse {

    private UUID id;
    private String invoiceNumber;
    private int itemCount;
    private BigDecimal amount;
    private Instant createdAt;
}
