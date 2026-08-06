package com.example.RetailOS.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RecentBillResponse {

    private UUID id;
    private String invoiceNumber;
    private String customerName;
    private int itemCount;
    private BigDecimal amount;
    private BillPaymentStatus status;
    private Instant createdAt;
}
