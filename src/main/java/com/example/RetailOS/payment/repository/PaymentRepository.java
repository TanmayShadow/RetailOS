package com.example.RetailOS.payment.repository;

import com.example.RetailOS.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByShopIdOrderByPaymentDateDescCreatedAtDesc(UUID shopId);

    List<Payment> findAllByShopIdAndCustomerIdOrderByPaymentDateDescCreatedAtDesc(
            UUID shopId, UUID customerId);

    List<Payment> findAllByShopIdAndInvoiceIdOrderByPaymentDateDescCreatedAtDesc(
            UUID shopId, UUID invoiceId);

    Optional<Payment> findByIdAndShopId(UUID id, UUID shopId);
}
