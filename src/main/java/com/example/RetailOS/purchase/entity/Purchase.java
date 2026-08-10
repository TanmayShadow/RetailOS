package com.example.RetailOS.purchase.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "purchases",
        indexes = {
                @Index(name = "idx_purchases_shop", columnList = "shop_id"),
                @Index(name = "idx_purchases_supplier", columnList = "supplier_id")
        }
)
public class Purchase extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
