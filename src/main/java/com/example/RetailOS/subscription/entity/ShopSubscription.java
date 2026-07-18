package com.example.RetailOS.subscription.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "shop_subscriptions",
        indexes = {
                @Index(name = "idx_shop_subscriptions_shop", columnList = "shop_id"),
                @Index(name = "idx_shop_subscriptions_plan", columnList = "plan_id")
        }
)
public class ShopSubscription extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Inclusive — the last day the subscription is valid.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
}
