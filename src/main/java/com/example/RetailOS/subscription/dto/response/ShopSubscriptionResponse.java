package com.example.RetailOS.subscription.dto.response;

import com.example.RetailOS.subscription.entity.BillingCycle;
import com.example.RetailOS.subscription.entity.ShopSubscription;
import com.example.RetailOS.subscription.entity.SubscriptionPlan;
import com.example.RetailOS.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class ShopSubscriptionResponse {

    private UUID id;
    private UUID shopId;
    private UUID planId;
    private String planName;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private Instant createdAt;

    public static ShopSubscriptionResponse from(ShopSubscription subscription, SubscriptionPlan plan) {
        return ShopSubscriptionResponse.builder()
                .id(subscription.getId())
                .shopId(subscription.getShopId())
                .planId(subscription.getPlanId())
                .planName(plan == null ? null : plan.getName())
                .price(plan == null ? null : plan.getPrice())
                .billingCycle(plan == null ? null : plan.getBillingCycle())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
