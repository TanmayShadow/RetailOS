package com.example.RetailOS.purchase.repository;

import com.example.RetailOS.purchase.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    List<PurchaseItem> findAllByPurchaseId(UUID purchaseId);

    void deleteAllByPurchaseId(UUID purchaseId);
}
