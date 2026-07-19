package com.example.RetailOS.invoice.entity;

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
        name = "invoices",
        indexes = {
                @Index(name = "idx_invoices_shop", columnList = "shop_id"),
                @Index(name = "idx_invoices_customer", columnList = "customer_id")
        }
)
public class Invoice extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    /**
     * Nullable — walk-in customers allowed.
     */
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
