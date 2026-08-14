package com.example.RetailOS.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReportSummaryResponse {

    private BigDecimal totalSales;
    /** Null when the prior period had zero sales — percent change is undefined. */
    private BigDecimal totalSalesDeltaPercent;

    private int billsCreated;
    /** Raw count difference vs. the prior period, not a percentage. */
    private int billsCreatedDelta;

    private BigDecimal grossProfit;
    private BigDecimal grossProfitMarginPercent;

    private BigDecimal avgBillValue;
    private BigDecimal avgBillValueDeltaPercent;
}
