package com.example.RetailOS.customer.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Partial update — only non-null fields are applied.
 * currentBalance is intentionally absent: the balance moves only through
 * invoice completion and payments, never a direct customer edit.
 */
@Getter
@Setter
public class UpdateCustomerRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;
}
