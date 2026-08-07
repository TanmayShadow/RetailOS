package com.example.RetailOS.product.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_shop", columnList = "shop_id"),
                @Index(name = "idx_products_category", columnList = "category_id")
        }
)
public class Product extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String sku;

    /**
     * The selling price is per this unit (e.g. per PIECE, per KG).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 10)
    @Builder.Default
    private ProductUnit unit = ProductUnit.PIECE;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    /**
     * Reference cost price shown next to selling price on the Products
     * screen. Independent of purchase_items.purchasePrice, which records
     * what was actually paid on each individual purchase transaction.
     */
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    /**
     * Settable once, as opening stock, when a product is created. After
     * that it only moves through the purchase/billing flows — never a
     * direct product update.
     */
    @Column(name = "current_stock", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    /**
     * Low-stock alert threshold. Zero (the default) means no alert is
     * raised for this product regardless of current stock.
     */
    @Column(name = "min_stock", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal minStock = BigDecimal.ZERO;
}
