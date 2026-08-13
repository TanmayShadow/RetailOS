package com.example.RetailOS.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DailySalesResponse {
    private LocalDate date;
    private BigDecimal totalSales;
}
