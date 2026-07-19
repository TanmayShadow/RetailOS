package com.example.RetailOS.invoice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Only product and quantity — the line total is always computed
 * server-side from the product's current selling price.
 */
@Getter
@Setter
public class InvoiceItemRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @DecimalMin(value = "0.001")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal quantity;
}
