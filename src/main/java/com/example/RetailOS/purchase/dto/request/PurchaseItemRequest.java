package com.example.RetailOS.purchase.dto.request;

import com.example.RetailOS.product.entity.ProductUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One line of a purchase. Reference an existing product via productId,
 * or create one inline by giving productName (+ sellingPrice, and
 * optionally sku / categoryId / categoryName) — no separate trip to the
 * product screen needed. If both are set, productId wins.
 */
@Getter
@Setter
public class PurchaseItemRequest {

    private UUID productId;

    @Size(min = 2, max = 150)
    private String productName;

    @Size(max = 50)
    private String sku;

    private UUID categoryId;

    @Size(max = 100)
    private String categoryName;

    /**
     * Only used when creating a product inline — defaults to PIECE.
     */
    private ProductUnit unit;

    @NotNull
    @DecimalMin(value = "0.001")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal purchasePrice;

    /**
     * Optional for an existing product — when set, updates the product's
     * selling price (a costlier restock usually means a new sale price).
     * Required when creating a product inline.
     */
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal sellingPrice;
}
