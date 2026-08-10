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

    /**
     * Sum of totalAmount across the customer's COMPLETED invoices, and the
     * createdAt of the most recent one — both derived from the invoice
     * ledger rather than stored, so they can never drift from it. Null
     * lastVisitAt means the customer has no completed purchases yet.
     */
    private BigDecimal totalPurchases;
    private Instant lastVisitAt;

    public static CustomerResponse from(Customer customer) {
        return from(customer, BigDecimal.ZERO, null);
    }

    public static CustomerResponse from(Customer customer, BigDecimal totalPurchases, Instant lastVisitAt) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .currentBalance(customer.getCurrentBalance())
                .createdAt(customer.getCreatedAt())
                .totalPurchases(totalPurchases)
                .lastVisitAt(lastVisitAt)
                .build();
    }
}
