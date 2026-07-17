package com.example.RetailOS.shop.repository;

import com.example.RetailOS.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
    Optional<Shop> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);
}
