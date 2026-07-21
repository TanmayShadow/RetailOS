package com.example.RetailOS.customer.entity;

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
        name = "customers",
        indexes = @Index(name = "idx_customers_shop", columnList = "shop_id")
)
public class Customer extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 15)
    private String phone;

    /**
     * Outstanding amount the customer owes (credit ledger). Increased when
     * an invoice for them is COMPLETED, decreased by payments — never
     * written from customer create/update requests.
     */
    @Column(name = "current_balance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;
}
