package com.example.RetailOS.purchase.repository;

import com.example.RetailOS.purchase.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    List<PurchaseItem> findAllByPurchaseId(UUID purchaseId);

    /**
     * Batch lookup so Purchase History can show an item count per row
     * without one query per purchase.
     */
    List<PurchaseItem> findAllByPurchaseIdIn(Collection<UUID> purchaseIds);

    void deleteAllByPurchaseId(UUID purchaseId);
}
