package com.example.RetailOS.payment.dto.response;

import com.example.RetailOS.payment.entity.Payment;
import com.example.RetailOS.payment.entity.PaymentMode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private UUID id;
    private UUID invoiceId;
    private UUID customerId;
    private String customerName;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private LocalDate paymentDate;
    private Instant createdAt;

    public static PaymentResponse from(Payment payment, String customerName) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoiceId())
                .customerId(payment.getCustomerId())
                .customerName(customerName)
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
