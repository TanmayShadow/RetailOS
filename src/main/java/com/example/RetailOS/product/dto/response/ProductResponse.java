package com.example.RetailOS.product.dto.response;

import com.example.RetailOS.category.entity.Category;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.entity.ProductUnit;
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
    private ProductUnit unit;
    private BigDecimal sellingPrice;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private UUID categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse from(Product product, Category category) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .unit(product.getUnit())
                .sellingPrice(product.getSellingPrice())
                .currentStock(product.getCurrentStock())
                .minStock(product.getMinStock())
                .categoryId(product.getCategoryId())
                .categoryName(category == null ? null : category.getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
