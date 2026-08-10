package com.example.RetailOS.invoice.dto.request;

import com.example.RetailOS.payment.entity.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * One-shot "finalize sale" — creates the invoice, decrements stock,
 * applies the discount, and (unless paying on credit) records full
 * payment, all in a single atomic call. There is no separate
 * draft/hold lifecycle behind this screen; in-progress bills live in
 * the browser until checkout.
 */
@Getter
@Setter
public class CheckoutRequest {

    /**
     * An existing customer selected from autocomplete. Takes priority
     * over customerName/customerPhone below.
     */
    private UUID customerId;

    /**
     * Only used when customerId is absent AND paymentMode is CREDIT —
     * a credit sale needs a real customer to attribute the balance to,
     * so one is found (by phone) or created from these fields. Ignored
     * for Cash/UPI/Card walk-in sales (never persisted).
     */
    @Size(max = 100)
    private String customerName;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String customerPhone;

    @NotEmpty
    @Valid
    private List<InvoiceItemRequest> items;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal discountAmount;

    @NotNull
    private PaymentMode paymentMode;
}
