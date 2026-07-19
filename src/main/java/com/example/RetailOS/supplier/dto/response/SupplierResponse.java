package com.example.RetailOS.supplier.dto.response;

import com.example.RetailOS.supplier.entity.Supplier;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class SupplierResponse {

    private UUID id;
    private String name;
    private String phone;
    private Instant createdAt;

    public static SupplierResponse from(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .phone(supplier.getPhone())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}
