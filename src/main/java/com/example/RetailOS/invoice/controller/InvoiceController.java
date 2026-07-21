package com.example.RetailOS.invoice.controller;

import com.example.RetailOS.invoice.dto.request.CreateInvoiceRequest;
import com.example.RetailOS.invoice.dto.request.UpdateInvoiceRequest;
import com.example.RetailOS.invoice.dto.response.InvoiceResponse;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import com.example.RetailOS.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('BILL_CREATE')")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BILL_VIEW')")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID customerId) {
        return ResponseEntity.ok(invoiceService.getInvoices(status, customerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BILL_VIEW')")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoice(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('BILL_CREATE')")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, request));
    }

    @PostMapping("/{id}/hold")
    @PreAuthorize("hasAuthority('BILL_CREATE')")
    public ResponseEntity<InvoiceResponse> holdInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.holdInvoice(id));
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('BILL_CREATE')")
    public ResponseEntity<InvoiceResponse> resumeInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.resumeInvoice(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('BILL_CREATE')")
    public ResponseEntity<InvoiceResponse> completeInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.completeInvoice(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('BILL_CANCEL')")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.cancelInvoice(id));
    }
}
