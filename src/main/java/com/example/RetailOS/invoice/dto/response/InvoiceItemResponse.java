package com.example.RetailOS.invoice.dto.response;

import com.example.RetailOS.invoice.entity.InvoiceItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class InvoiceItemResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal lineTotal;

    public static InvoiceItemResponse from(InvoiceItem item, String productName) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(productName)
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
