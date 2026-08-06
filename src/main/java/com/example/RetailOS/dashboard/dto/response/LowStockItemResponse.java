package com.example.RetailOS.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class LowStockItemResponse {

    private UUID id;
    private String name;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private StockSeverity severity;
}
