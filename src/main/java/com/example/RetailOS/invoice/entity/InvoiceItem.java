package com.example.RetailOS.invoice.entity;

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
        name = "invoice_items",
        indexes = {
                @Index(name = "idx_invoice_items_invoice", columnList = "invoice_id"),
                @Index(name = "idx_invoice_items_product", columnList = "product_id")
        }
)
public class InvoiceItem extends BaseEntity {

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    /**
     * Nullable in the DB so billing history survives product deletion
     * (FK is ON DELETE SET NULL) — always set when the item is created.
     */
    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}
