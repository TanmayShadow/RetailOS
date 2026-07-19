package com.example.RetailOS.supplier.controller;

import com.example.RetailOS.supplier.dto.request.CreateSupplierRequest;
import com.example.RetailOS.supplier.dto.request.UpdateSupplierRequest;
import com.example.RetailOS.supplier.dto.response.SupplierResponse;
import com.example.RetailOS.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW')")
    public ResponseEntity<List<SupplierResponse>> getSuppliers() {
        return ResponseEntity.ok(supplierService.getSuppliers());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
