package com.example.RetailOS.supplier.repository;

import com.example.RetailOS.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    List<Supplier> findAllByShopIdOrderByNameAsc(UUID shopId);

    Optional<Supplier> findByIdAndShopId(UUID id, UUID shopId);

    Optional<Supplier> findByShopIdAndNameIgnoreCase(UUID shopId, String name);

    boolean existsByShopIdAndNameIgnoreCase(UUID shopId, String name);

    boolean existsByShopIdAndNameIgnoreCaseAndIdNot(UUID shopId, String name, UUID id);
}
