package com.example.RetailOS.category.dto.response;

import com.example.RetailOS.category.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CategoryResponse {

    private UUID id;
    private String name;
    private Instant createdAt;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
