package com.example.RetailOS.purchase.controller;

import com.example.RetailOS.purchase.dto.request.CreatePurchaseRequest;
import com.example.RetailOS.purchase.dto.response.PurchaseResponse;
import com.example.RetailOS.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(purchaseService.createPurchase(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<PurchaseResponse>> getPurchases(@RequestParam(required = false) UUID supplierId) {
        return ResponseEntity.ok(purchaseService.getPurchases(supplierId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<PurchaseResponse> getPurchase(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseService.getPurchase(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deletePurchase(@PathVariable UUID id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }
}
