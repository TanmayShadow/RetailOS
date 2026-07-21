package com.example.RetailOS.purchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreatePurchaseRequest {

    /**
     * Either reference an existing supplier by id, or give a name to
     * find-or-create one. Both may be omitted (e.g. a cash-and-carry
     * purchase). If both are set, supplierId wins.
     */
    private UUID supplierId;

    @Size(max = 100)
    private String supplierName;

    /**
     * Optional — defaults to today.
     */
    private LocalDate purchaseDate;

    @NotEmpty
    @Valid
    private List<PurchaseItemRequest> items;
}
