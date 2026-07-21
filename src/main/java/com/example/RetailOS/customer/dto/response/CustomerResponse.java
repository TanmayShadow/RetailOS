package com.example.RetailOS.customer.dto.response;

import com.example.RetailOS.customer.entity.Customer;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CustomerResponse {

    private UUID id;
    private String name;
    private String phone;
    private BigDecimal currentBalance;
    private Instant createdAt;

    public static CustomerResponse from(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .currentBalance(customer.getCurrentBalance())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
