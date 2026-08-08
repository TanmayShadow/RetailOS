package com.example.RetailOS.product.dto.request;

import com.example.RetailOS.product.entity.ProductUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Partial update — only non-null fields are applied.
 * currentStock is intentionally absent: stock moves only through
 * purchase/billing flows, never a direct product edit.
 */
@Getter
@Setter
public class UpdateProductRequest {

    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 50)
    private String sku;

    private ProductUnit unit;

    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.00")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal minStock;

    private UUID categoryId;

    @Size(max = 100)
    private String categoryName;

    /**
     * Set true to detach the product from its category.
     */
    private Boolean removeCategory;
}
