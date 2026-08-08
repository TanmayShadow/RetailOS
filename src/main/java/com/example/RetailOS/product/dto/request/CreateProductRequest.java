package com.example.RetailOS.product.dto.request;

import com.example.RetailOS.product.entity.ProductUnit;
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

    /**
     * Optional — defaults to PIECE. Selling price is per this unit.
     */
    private ProductUnit unit;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal sellingPrice;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal purchasePrice;

    /**
     * Opening stock — only settable here, at creation. After this the
     * product's stock only moves through the purchase/billing flows.
     * Optional — defaults to 0.
     */
    @DecimalMin(value = "0.00")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal openingStock;

    /**
     * Low-stock alert threshold. Optional — defaults to 0 (no alert).
     */
    @DecimalMin(value = "0.00")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal minStock;

    /**
     * Either reference an existing category by id, or give a name to
     * find-or-create one. If both are set, categoryId wins.
     */
    private UUID categoryId;

    @Size(max = 100)
    private String categoryName;
}
