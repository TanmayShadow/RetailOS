package com.example.RetailOS.invoice.dto.response;

import com.example.RetailOS.invoice.entity.InvoiceItem;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.entity.ProductUnit;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Builder
public class InvoiceItemResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private ProductUnit unit;
    private BigDecimal quantity;
    /** Per-unit price — back-derived from lineTotal/quantity for display (receipts, WhatsApp, print). */
    private BigDecimal rate;
    private BigDecimal lineTotal;

    public static InvoiceItemResponse from(InvoiceItem item, Product product) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(product == null ? null : product.getName())
                .unit(product == null ? null : product.getUnit())
                .quantity(item.getQuantity())
                .rate(item.getQuantity().signum() == 0
                        ? BigDecimal.ZERO
                        : item.getLineTotal().divide(item.getQuantity(), 2, RoundingMode.HALF_UP))
                .lineTotal(item.getLineTotal())
                .build();
    }
}
