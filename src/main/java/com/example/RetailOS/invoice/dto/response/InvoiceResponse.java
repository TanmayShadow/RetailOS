package com.example.RetailOS.invoice.dto.response;

import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class InvoiceResponse {

    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private InvoiceStatus status;
    private BigDecimal totalAmount;
    private UUID createdBy;
    private Instant createdAt;

    /**
     * Populated on the detail endpoint; null in list responses.
     */
    private List<InvoiceItemResponse> items;

    public static InvoiceResponse from(Invoice invoice, String customerName,
                                       List<InvoiceItemResponse> items) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomerId())
                .customerName(customerName)
                .status(invoice.getStatus())
                .totalAmount(invoice.getTotalAmount())
                .createdBy(invoice.getCreatedBy())
                .createdAt(invoice.getCreatedAt())
                .items(items)
                .build();
    }
}
