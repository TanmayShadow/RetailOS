package com.example.RetailOS.subscription.controller;

import com.example.RetailOS.subscription.dto.request.CreateShopSubscriptionRequest;
import com.example.RetailOS.subscription.dto.request.UpdateSubscriptionStatusRequest;
import com.example.RetailOS.subscription.dto.response.ShopSubscriptionResponse;
import com.example.RetailOS.subscription.service.ShopSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shop-subscriptions")
@RequiredArgsConstructor
public class ShopSubscriptionController {

    private final ShopSubscriptionService shopSubscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopSubscriptionResponse> subscribe(
            @Valid @RequestBody CreateShopSubscriptionRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(shopSubscriptionService.subscribe(request));
    }

    @GetMapping("/current")
    public ResponseEntity<ShopSubscriptionResponse> getCurrentSubscription() {
        return ResponseEntity.ok(shopSubscriptionService.getCurrentSubscription());
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ShopSubscriptionResponse>> getMySubscriptions() {
        return ResponseEntity.ok(shopSubscriptionService.getMySubscriptions());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopSubscriptionResponse> cancelSubscription(@PathVariable UUID id) {
        return ResponseEntity.ok(shopSubscriptionService.cancelSubscription(id));
    }

    // ---- Platform admin ----

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_VIEW_ALL')")
    public ResponseEntity<List<ShopSubscriptionResponse>> getSubscriptionsForShop(
            @PathVariable UUID shopId) {
        return ResponseEntity.ok(shopSubscriptionService.getSubscriptionsForShop(shopId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_UPDATE')")
    public ResponseEntity<ShopSubscriptionResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionStatusRequest request) {
        return ResponseEntity.ok(shopSubscriptionService.updateStatus(id, request));
    }
}
