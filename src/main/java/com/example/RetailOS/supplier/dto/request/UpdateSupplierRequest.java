package com.example.RetailOS.supplier.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Partial update — only non-null fields are applied.
 */
@Getter
@Setter
public class UpdateSupplierRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;
}
