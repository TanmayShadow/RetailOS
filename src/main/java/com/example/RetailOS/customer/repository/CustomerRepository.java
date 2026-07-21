package com.example.RetailOS.customer.repository;

import com.example.RetailOS.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByShopIdOrderByNameAsc(UUID shopId);

    List<Customer> findAllByShopIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID shopId, String name);

    List<Customer> findAllByShopIdAndPhoneContainingOrderByNameAsc(UUID shopId, String phone);

    Optional<Customer> findByIdAndShopId(UUID id, UUID shopId);

    boolean existsByShopIdAndPhone(UUID shopId, String phone);

    boolean existsByShopIdAndPhoneAndIdNot(UUID shopId, String phone, UUID id);
}
