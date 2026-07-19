package com.example.RetailOS.purchase.dto.response;

import com.example.RetailOS.purchase.entity.PurchaseItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class PurchaseItemResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private BigDecimal subtotal;

    public static PurchaseItemResponse from(PurchaseItem item, String productName) {
        return PurchaseItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(productName)
                .quantity(item.getQuantity())
                .purchasePrice(item.getPurchasePrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
