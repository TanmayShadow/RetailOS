package com.example.RetailOS.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class TopProductResponse {
    private UUID productId;
    private String productName;
    private BigDecimal unitsSold;
    private BigDecimal revenue;
    /**
     * revenue minus (unitsSold × the product's *current* purchase price) —
     * invoice items don't snapshot a cost basis at sale time, so this uses
     * today's purchase price rather than what was actually paid back then.
     * Fine for a quick read, not historically exact after a price change.
     */
    private BigDecimal profit;
}
