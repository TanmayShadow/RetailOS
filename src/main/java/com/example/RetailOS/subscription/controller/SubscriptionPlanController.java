package com.example.RetailOS.subscription.controller;

import com.example.RetailOS.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.example.RetailOS.subscription.dto.request.UpdateSubscriptionPlanRequest;
import com.example.RetailOS.subscription.dto.response.SubscriptionPlanResponse;
import com.example.RetailOS.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    @PreAuthorize("hasAuthority('PLAN_CREATE')")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subscriptionPlanService.createPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanResponse>> getActivePlans() {
        return ResponseEntity.ok(subscriptionPlanService.getActivePlans());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('PLAN_VIEW_ALL')")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(subscriptionPlanService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> getPlan(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionPlanService.getPlan(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PLAN_UPDATE')")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(subscriptionPlanService.updatePlan(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PLAN_DELETE')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable UUID id) {
        subscriptionPlanService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }
}
