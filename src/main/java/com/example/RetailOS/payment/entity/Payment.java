package com.example.RetailOS.payment.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_shop", columnList = "shop_id"),
                @Index(name = "idx_payments_invoice", columnList = "invoice_id"),
                @Index(name = "idx_payments_customer", columnList = "customer_id")
        }
)
public class Payment extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 6)
    @Builder.Default
    private PaymentMode paymentMode = PaymentMode.CASH;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
}
