package com.example.RetailOS.product.dto.response;

import com.example.RetailOS.category.entity.Category;
import com.example.RetailOS.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private String sku;
    private BigDecimal sellingPrice;
    private BigDecimal currentStock;
    private UUID categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse from(Product product, Category category) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .sellingPrice(product.getSellingPrice())
                .currentStock(product.getCurrentStock())
                .categoryId(product.getCategoryId())
                .categoryName(category == null ? null : category.getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
