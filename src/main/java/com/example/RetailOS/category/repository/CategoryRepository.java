package com.example.RetailOS.category.repository;

import com.example.RetailOS.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByShopIdOrderByNameAsc(UUID shopId);

    Optional<Category> findByIdAndShopId(UUID id, UUID shopId);

    Optional<Category> findByShopIdAndNameIgnoreCase(UUID shopId, String name);

    boolean existsByShopIdAndNameIgnoreCase(UUID shopId, String name);

    boolean existsByShopIdAndNameIgnoreCaseAndIdNot(UUID shopId, String name, UUID id);
}
