package com.example.RetailOS.payment.repository;

import com.example.RetailOS.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByShopIdOrderByPaymentDateDescCreatedAtDesc(UUID shopId);

    /**
     * Reports' payment-methods breakdown: what was actually collected by
     * each method in a date range, based on when the payment was recorded
     * rather than when the underlying invoice was created (a credit
     * repayment collected today counts as today's cash-in, even against
     * an older invoice).
     */
    List<Payment> findAllByShopIdAndPaymentDateBetween(UUID shopId, LocalDate from, LocalDate to);

    /**
     * Batch lookup so the dashboard's recent-bills widget can derive a
     * paid-so-far amount per invoice without one query per row.
     */
    List<Payment> findAllByInvoiceIdIn(Collection<UUID> invoiceIds);

    List<Payment> findAllByShopIdAndCustomerIdOrderByPaymentDateDescCreatedAtDesc(
            UUID shopId, UUID customerId);

    List<Payment> findAllByShopIdAndInvoiceIdOrderByPaymentDateDescCreatedAtDesc(
            UUID shopId, UUID invoiceId);

    Optional<Payment> findByIdAndShopId(UUID id, UUID shopId);
}
