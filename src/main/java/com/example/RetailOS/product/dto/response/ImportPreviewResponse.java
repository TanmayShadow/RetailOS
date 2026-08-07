package com.example.RetailOS.product.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImportPreviewResponse {

    private List<ImportRowResult> rows;
    private int validCount;
    private int duplicateCount;
    private int errorCount;
}
