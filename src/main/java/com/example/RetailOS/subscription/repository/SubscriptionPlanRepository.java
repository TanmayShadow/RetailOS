package com.example.RetailOS.subscription.repository;

import com.example.RetailOS.subscription.entity.BillingCycle;
import com.example.RetailOS.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    List<SubscriptionPlan> findAllByIsActiveTrueOrderByPriceAsc();

    boolean existsByNameIgnoreCaseAndBillingCycle(String name, BillingCycle billingCycle);

    boolean existsByNameIgnoreCaseAndBillingCycleAndIdNot(String name, BillingCycle billingCycle, UUID id);
}
