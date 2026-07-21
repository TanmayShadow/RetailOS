package com.example.RetailOS.purchase.dto.response;

import com.example.RetailOS.purchase.entity.Purchase;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class PurchaseResponse {

    private UUID id;
    private UUID supplierId;
    private String supplierName;
    private LocalDate purchaseDate;
    private BigDecimal totalAmount;
    private UUID createdBy;
    private Instant createdAt;

    /**
     * Populated on the detail endpoint; null in list responses.
     */
    private List<PurchaseItemResponse> items;

    public static PurchaseResponse from(Purchase purchase, String supplierName,
                                        List<PurchaseItemResponse> items) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplierId(purchase.getSupplierId())
                .supplierName(supplierName)
                .purchaseDate(purchase.getPurchaseDate())
                .totalAmount(purchase.getTotalAmount())
                .createdBy(purchase.getCreatedBy())
                .createdAt(purchase.getCreatedAt())
                .items(items)
                .build();
    }
}
