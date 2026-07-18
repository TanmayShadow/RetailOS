package com.example.RetailOS.subscription.dto.response;

import com.example.RetailOS.subscription.entity.BillingCycle;
import com.example.RetailOS.subscription.entity.SubscriptionPlan;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionPlanResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static SubscriptionPlanResponse from(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .billingCycle(plan.getBillingCycle())
                .isActive(plan.isActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
