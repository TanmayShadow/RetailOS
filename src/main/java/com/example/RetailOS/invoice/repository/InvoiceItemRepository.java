package com.example.RetailOS.invoice.repository;

import com.example.RetailOS.invoice.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findAllByInvoiceId(UUID invoiceId);

    void deleteAllByInvoiceId(UUID invoiceId);
}
