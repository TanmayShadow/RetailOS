package com.example.RetailOS.subscription.service;

import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.example.RetailOS.subscription.dto.request.UpdateSubscriptionPlanRequest;
import com.example.RetailOS.subscription.dto.response.SubscriptionPlanResponse;
import com.example.RetailOS.subscription.entity.BillingCycle;
import com.example.RetailOS.subscription.entity.SubscriptionPlan;
import com.example.RetailOS.subscription.repository.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request) {

        String name = request.getName().trim();
        BillingCycle billingCycle = request.getBillingCycle() == null
                ? BillingCycle.ANNUAL : request.getBillingCycle();

        if (subscriptionPlanRepository.existsByNameIgnoreCaseAndBillingCycle(name, billingCycle)) {
            throw new ResourceAlreadyExistsException(
                    "A " + billingCycle + " plan with this name already exists.");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(name)
                .price(request.getPrice())
                .billingCycle(billingCycle)
                .build();

        return SubscriptionPlanResponse.from(subscriptionPlanRepository.save(plan));
    }

    public SubscriptionPlanResponse getPlan(UUID id) {
        return SubscriptionPlanResponse.from(requirePlan(id));
    }

    public List<SubscriptionPlanResponse> getActivePlans() {
        return subscriptionPlanRepository.findAllByIsActiveTrueOrderByPriceAsc()
                .stream()
                .map(SubscriptionPlanResponse::from)
                .toList();
    }

    public List<SubscriptionPlanResponse> getAllPlans() {
        return subscriptionPlanRepository.findAll()
                .stream()
                .map(SubscriptionPlanResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionPlanResponse updatePlan(UUID id, UpdateSubscriptionPlanRequest request) {

        SubscriptionPlan plan = requirePlan(id);

        String name = request.getName() == null ? plan.getName() : request.getName().trim();
        BillingCycle billingCycle = request.getBillingCycle() == null
                ? plan.getBillingCycle() : request.getBillingCycle();

        if (subscriptionPlanRepository
                .existsByNameIgnoreCaseAndBillingCycleAndIdNot(name, billingCycle, id)) {
            throw new ResourceAlreadyExistsException(
                    "A " + billingCycle + " plan with this name already exists.");
        }

        plan.setName(name);
        plan.setBillingCycle(billingCycle);
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getIsActive() != null) {
            plan.setActive(request.getIsActive());
        }

        return SubscriptionPlanResponse.from(subscriptionPlanRepository.save(plan));
    }

    /**
     * Soft delete — plans may be referenced by shop subscriptions,
     * so they are deactivated rather than removed.
     */
    @Transactional
    public void deactivatePlan(UUID id) {
        SubscriptionPlan plan = requirePlan(id);
        plan.setActive(false);
        subscriptionPlanRepository.save(plan);
    }

    private SubscriptionPlan requirePlan(UUID id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription plan not found: " + id));
    }
}
