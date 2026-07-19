package com.example.RetailOS.invoice.dto.response;

import com.example.RetailOS.invoice.entity.InvoiceItem;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.entity.ProductUnit;
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
    private ProductUnit unit;
    private BigDecimal quantity;
    private BigDecimal lineTotal;

    public static InvoiceItemResponse from(InvoiceItem item, Product product) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(product == null ? null : product.getName())
                .unit(product == null ? null : product.getUnit())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
