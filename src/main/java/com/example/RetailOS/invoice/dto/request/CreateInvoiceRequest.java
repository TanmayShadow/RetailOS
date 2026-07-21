package com.example.RetailOS.invoice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateInvoiceRequest {

    /**
     * Optional — null means a walk-in customer.
     */
    private UUID customerId;

    @NotEmpty
    @Valid
    private List<InvoiceItemRequest> items;
}
