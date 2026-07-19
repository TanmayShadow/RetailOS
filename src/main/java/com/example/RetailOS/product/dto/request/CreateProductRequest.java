package com.example.RetailOS.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 50)
    private String sku;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal sellingPrice;

    /**
     * Either reference an existing category by id, or give a name to
     * find-or-create one. If both are set, categoryId wins.
     */
    private UUID categoryId;

    @Size(max = 100)
    private String categoryName;
}
