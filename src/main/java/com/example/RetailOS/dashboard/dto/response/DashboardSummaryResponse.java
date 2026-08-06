package com.example.RetailOS.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardSummaryResponse {

    private BigDecimal salesToday;
    private BigDecimal salesYesterday;

    /**
     * Percentage change of salesToday vs salesYesterday. Null when
     * yesterday had no sales (undefined percentage change).
     */
    private BigDecimal salesChangePercent;

    private int billsToday;
    private int billsYesterday;
    private int billsDelta;

    private BigDecimal creditPending;
    private int creditPendingCustomerCount;

    private int lowStockCount;
    private int veryLowStockCount;
}
