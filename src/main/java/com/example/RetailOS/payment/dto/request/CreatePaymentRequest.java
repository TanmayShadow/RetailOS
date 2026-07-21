package com.example.RetailOS.payment.dto.request;

import com.example.RetailOS.payment.entity.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreatePaymentRequest {

    /**
     * Optional — a payment can settle a specific invoice, or be a
     * standalone credit repayment against the customer's balance.
     * When only invoiceId is given, the customer is taken from the invoice.
     */
    private UUID invoiceId;

    private UUID customerId;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    /**
     * Optional — defaults to CASH.
     */
    private PaymentMode paymentMode;

    /**
     * Optional — defaults to today.
     */
    private LocalDate paymentDate;
}
