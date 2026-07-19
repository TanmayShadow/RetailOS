package com.example.RetailOS.purchase.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "purchase_items",
        indexes = {
                @Index(name = "idx_purchase_items_purchase", columnList = "purchase_id"),
                @Index(name = "idx_purchase_items_product", columnList = "product_id")
        }
)
public class PurchaseItem extends BaseEntity {

    @Column(name = "purchase_id", nullable = false)
    private UUID purchaseId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
