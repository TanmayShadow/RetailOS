package com.example.RetailOS.subscription.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 10)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.ANNUAL;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
