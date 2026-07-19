package com.example.RetailOS.invoice.dto.request;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Only DRAFT and HELD invoices can be edited. Null fields are left
 * unchanged; a non-null items list REPLACES all existing items.
 */
@Getter
@Setter
public class UpdateInvoiceRequest {

    private UUID customerId;

    /**
     * Set true to detach the invoice from its customer (walk-in).
     */
    private Boolean removeCustomer;

    @Valid
    private List<InvoiceItemRequest> items;
}
