package com.example.RetailOS.report.controller;

import com.example.RetailOS.report.dto.response.DailySalesResponse;
import com.example.RetailOS.report.dto.response.PaymentMethodBreakdownResponse;
import com.example.RetailOS.report.dto.response.ReportSummaryResponse;
import com.example.RetailOS.report.dto.response.TopProductResponse;
import com.example.RetailOS.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ReportSummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getSummary(from, to));
    }

    @GetMapping("/sales-trend")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DailySalesResponse>> getSalesTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getSalesTrend(from, to));
    }

    @GetMapping("/payment-methods")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<PaymentMethodBreakdownResponse>> getPaymentMethods(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getPaymentMethods(from, to));
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<TopProductResponse>> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getTopProducts(from, to, limit));
    }
}
