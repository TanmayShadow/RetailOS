package com.example.RetailOS.customer.repository;

import com.example.RetailOS.customer.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByShopIdOrderByNameAsc(UUID shopId);

    List<Customer> findAllByShopIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID shopId, String name);

    /**
     * Most recently active customers (balance changes bump updatedAt) for
     * the dashboard's recent-customers widget.
     */
    List<Customer> findAllByShopIdOrderByUpdatedAtDesc(UUID shopId, Pageable pageable);

    /**
     * Customers currently carrying a credit balance, for the dashboard's
     * credit-pending stat.
     */
    List<Customer> findAllByShopIdAndCurrentBalanceGreaterThanOrderByCurrentBalanceDesc(
            UUID shopId, BigDecimal balance);

    List<Customer> findAllByShopIdAndPhoneContainingOrderByNameAsc(UUID shopId, String phone);

    Optional<Customer> findByIdAndShopId(UUID id, UUID shopId);

    boolean existsByShopIdAndPhone(UUID shopId, String phone);

    boolean existsByShopIdAndPhoneAndIdNot(UUID shopId, String phone, UUID id);
}
