package com.example.RetailOS.product.repository;

import com.example.RetailOS.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByShopIdOrderByNameAsc(UUID shopId);

    List<Product> findAllByShopIdAndCategoryIdOrderByNameAsc(UUID shopId, UUID categoryId);

    List<Product> findAllByShopIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID shopId, String name);

    Optional<Product> findByIdAndShopId(UUID id, UUID shopId);

    boolean existsByShopIdAndSkuIgnoreCase(UUID shopId, String sku);

    boolean existsByShopIdAndSkuIgnoreCaseAndIdNot(UUID shopId, String sku, UUID id);
}
