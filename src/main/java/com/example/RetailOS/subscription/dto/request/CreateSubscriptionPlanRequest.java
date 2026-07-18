package com.example.RetailOS.subscription.dto.request;

import com.example.RetailOS.subscription.entity.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSubscriptionPlanRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    private BillingCycle billingCycle;
}
