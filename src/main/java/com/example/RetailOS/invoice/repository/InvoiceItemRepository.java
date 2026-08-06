package com.example.RetailOS.invoice.repository;

import com.example.RetailOS.invoice.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findAllByInvoiceId(UUID invoiceId);

    /**
     * Batch lookup so the dashboard's recent-bills widget can derive an
     * item count per invoice without one query per row.
     */
    List<InvoiceItem> findAllByInvoiceIdIn(Collection<UUID> invoiceIds);

    void deleteAllByInvoiceId(UUID invoiceId);
}
