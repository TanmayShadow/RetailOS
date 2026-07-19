package com.example.RetailOS.subscription.repository;

import com.example.RetailOS.subscription.entity.ShopSubscription;
import com.example.RetailOS.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopSubscriptionRepository extends JpaRepository<ShopSubscription, UUID> {

    List<ShopSubscription> findAllByShopIdOrderByCreatedAtDesc(UUID shopId);

    Optional<ShopSubscription> findByIdAndShopId(UUID id, UUID shopId);

    Optional<ShopSubscription> findFirstByShopIdAndStatusInAndEndDateGreaterThanEqualOrderByEndDateDesc(
            UUID shopId, Collection<SubscriptionStatus> statuses, LocalDate date);

    boolean existsByShopIdAndStatusInAndEndDateGreaterThanEqual(
            UUID shopId, Collection<SubscriptionStatus> statuses, LocalDate date);
}
