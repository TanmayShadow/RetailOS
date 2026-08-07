package com.example.RetailOS.product.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImportCommitResponse {

    private int createdCount;
    private int failedCount;
    private List<String> errors;
}
