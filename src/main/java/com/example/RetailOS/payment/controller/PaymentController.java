package com.example.RetailOS.payment.controller;

import com.example.RetailOS.payment.dto.request.CreatePaymentRequest;
import com.example.RetailOS.payment.dto.response.PaymentResponse;
import com.example.RetailOS.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAuthority('BILL_CREATE')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BILL_VIEW')")
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID invoiceId) {
        return ResponseEntity.ok(paymentService.getPayments(customerId, invoiceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BILL_VIEW')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }
}
