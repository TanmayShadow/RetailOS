package com.example.RetailOS.purchase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PurchaseItemRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @DecimalMin(value = "0.001")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal purchasePrice;
}
