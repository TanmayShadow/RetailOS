package com.example.RetailOS.invoice.repository;

import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findAllByShopIdOrderByCreatedAtDesc(UUID shopId);

    List<Invoice> findAllByShopIdAndStatusOrderByCreatedAtDesc(UUID shopId, InvoiceStatus status);

    /**
     * Used by the dashboard for today's/yesterday's stats and the
     * recent-bills widget (bounded to a single day's invoices for a shop).
     */
    List<Invoice> findAllByShopIdAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID shopId, InvoiceStatus status, Instant start, Instant end);

    List<Invoice> findAllByShopIdAndCustomerIdOrderByCreatedAtDesc(UUID shopId, UUID customerId);

    Optional<Invoice> findByIdAndShopId(UUID id, UUID shopId);

    boolean existsByShopIdAndInvoiceNumber(UUID shopId, String invoiceNumber);

    long countByShopId(UUID shopId);
}
