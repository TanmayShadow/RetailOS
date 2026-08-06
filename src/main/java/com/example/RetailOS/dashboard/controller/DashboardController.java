package com.example.RetailOS.dashboard.controller;

import com.example.RetailOS.dashboard.dto.response.DashboardSummaryResponse;
import com.example.RetailOS.dashboard.dto.response.LowStockItemResponse;
import com.example.RetailOS.dashboard.dto.response.RecentBillResponse;
import com.example.RetailOS.dashboard.dto.response.RecentCustomerResponse;
import com.example.RetailOS.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Home-screen widgets. Every authenticated role lands on the dashboard
 * (including CASHIER, which lacks REPORT_VIEW), so these endpoints are
 * intentionally not permission-gated beyond authentication.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/recent-bills")
    public ResponseEntity<List<RecentBillResponse>> getRecentBills(
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentBills(limit));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockItemResponse>> getLowStockItems(
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(dashboardService.getLowStockItems(limit));
    }

    @GetMapping("/recent-customers")
    public ResponseEntity<List<RecentCustomerResponse>> getRecentCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentCustomers(limit));
    }
}
