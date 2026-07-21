package com.example.RetailOS.purchase.repository;

import com.example.RetailOS.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    List<Purchase> findAllByShopIdOrderByPurchaseDateDescCreatedAtDesc(UUID shopId);

    List<Purchase> findAllByShopIdAndSupplierIdOrderByPurchaseDateDescCreatedAtDesc(
            UUID shopId, UUID supplierId);

    Optional<Purchase> findByIdAndShopId(UUID id, UUID shopId);
}
