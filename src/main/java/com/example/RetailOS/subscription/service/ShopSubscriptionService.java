package com.example.RetailOS.subscription.service;

import com.example.RetailOS.platform.exception.BadRequestException;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.subscription.dto.request.CreateShopSubscriptionRequest;
import com.example.RetailOS.subscription.dto.request.UpdateSubscriptionStatusRequest;
import com.example.RetailOS.subscription.dto.response.ShopSubscriptionResponse;
import com.example.RetailOS.subscription.entity.BillingCycle;
import com.example.RetailOS.subscription.entity.ShopSubscription;
import com.example.RetailOS.subscription.entity.SubscriptionPlan;
import com.example.RetailOS.subscription.entity.SubscriptionStatus;
import com.example.RetailOS.subscription.repository.ShopSubscriptionRepository;
import com.example.RetailOS.subscription.repository.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopSubscriptionService {

    /**
     * Statuses that occupy the shop's subscription slot — a shop with a
     * non-expired subscription in one of these cannot subscribe again.
     */
    private static final Set<SubscriptionStatus> LIVE_STATUSES =
            Set.of(SubscriptionStatus.TRIAL, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE);

    private final ShopSubscriptionRepository shopSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ShopSubscriptionResponse subscribe(CreateShopSubscriptionRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription plan not found: " + request.getPlanId()));
        if (!plan.isActive()) {
            throw new BadRequestException("This plan is no longer available.");
        }

        if (shopSubscriptionRepository.existsByShopIdAndStatusInAndEndDateGreaterThanEqual(
                shopId, LIVE_STATUSES, LocalDate.now())) {
            throw new ResourceAlreadyExistsException(
                    "This shop already has an active subscription.");
        }

        LocalDate startDate = request.getStartDate() == null
                ? LocalDate.now() : request.getStartDate();
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past.");
        }

        ShopSubscription subscription = ShopSubscription.builder()
                .shopId(shopId)
                .planId(plan.getId())
                .startDate(startDate)
                .endDate(computeEndDate(startDate, plan.getBillingCycle()))
                .build();

        return ShopSubscriptionResponse.from(
                shopSubscriptionRepository.save(subscription), plan);
    }

    public ShopSubscriptionResponse getCurrentSubscription() {
        UUID shopId = currentUserService.getCurrentShopId();
        ShopSubscription subscription = shopSubscriptionRepository
                .findFirstByShopIdAndStatusInAndEndDateGreaterThanEqualOrderByEndDateDesc(
                        shopId, LIVE_STATUSES, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active subscription for this shop."));
        return toResponse(subscription);
    }

    public List<ShopSubscriptionResponse> getMySubscriptions() {
        return toResponses(shopSubscriptionRepository
                .findAllByShopIdOrderByCreatedAtDesc(currentUserService.getCurrentShopId()));
    }

    @Transactional
    public ShopSubscriptionResponse cancelSubscription(UUID id) {

        ShopSubscription subscription = shopSubscriptionRepository
                .findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + id));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BadRequestException("Subscription is already cancelled.");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        return toResponse(shopSubscriptionRepository.save(subscription));
    }

    // ---- Platform admin ----

    public List<ShopSubscriptionResponse> getSubscriptionsForShop(UUID shopId) {
        return toResponses(shopSubscriptionRepository
                .findAllByShopIdOrderByCreatedAtDesc(shopId));
    }

    @Transactional
    public ShopSubscriptionResponse updateStatus(UUID id, UpdateSubscriptionStatusRequest request) {
        ShopSubscription subscription = shopSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + id));
        subscription.setStatus(request.getStatus());
        return toResponse(shopSubscriptionRepository.save(subscription));
    }

    // ---- Helpers ----

    /**
     * End date is inclusive: a MONTHLY plan started on Jan 15 runs
     * through Feb 14, an ANNUAL one through Jan 14 next year.
     */
    private LocalDate computeEndDate(LocalDate startDate, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? startDate.plusMonths(1).minusDays(1)
                : startDate.plusYears(1).minusDays(1);
    }

    private ShopSubscriptionResponse toResponse(ShopSubscription subscription) {
        SubscriptionPlan plan = subscriptionPlanRepository
                .findById(subscription.getPlanId()).orElse(null);
        return ShopSubscriptionResponse.from(subscription, plan);
    }

    private List<ShopSubscriptionResponse> toResponses(List<ShopSubscription> subscriptions) {
        Map<UUID, SubscriptionPlan> plansById = subscriptionPlanRepository
                .findAllById(subscriptions.stream().map(ShopSubscription::getPlanId).toList())
                .stream()
                .collect(Collectors.toMap(SubscriptionPlan::getId, Function.identity()));
        return subscriptions.stream()
                .map(s -> ShopSubscriptionResponse.from(s, plansById.get(s.getPlanId())))
                .toList();
    }
}
