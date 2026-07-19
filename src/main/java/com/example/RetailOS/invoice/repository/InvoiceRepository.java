package com.example.RetailOS.invoice.repository;

import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findAllByShopIdOrderByCreatedAtDesc(UUID shopId);

    List<Invoice> findAllByShopIdAndStatusOrderByCreatedAtDesc(UUID shopId, InvoiceStatus status);

    List<Invoice> findAllByShopIdAndCustomerIdOrderByCreatedAtDesc(UUID shopId, UUID customerId);

    Optional<Invoice> findByIdAndShopId(UUID id, UUID shopId);

    boolean existsByShopIdAndInvoiceNumber(UUID shopId, String invoiceNumber);

    long countByShopId(UUID shopId);
}
