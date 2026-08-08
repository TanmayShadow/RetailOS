package com.example.RetailOS.product.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ImportRowResult {

    /** 1-based, matching the spreadsheet row excluding the header. */
    private int rowNumber;

    private String name;
    private String sku;
    private String categoryName;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal openingStock;
    private BigDecimal minStock;

    private ImportRowStatus status;
    private List<String> messages;
}
