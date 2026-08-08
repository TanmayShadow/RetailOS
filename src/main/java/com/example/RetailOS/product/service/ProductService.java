package com.example.RetailOS.product.service;

import com.example.RetailOS.category.entity.Category;
import com.example.RetailOS.category.repository.CategoryRepository;
import com.example.RetailOS.category.service.CategoryService;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.product.dto.request.CreateProductRequest;
import com.example.RetailOS.product.dto.request.UpdateProductRequest;
import com.example.RetailOS.product.dto.response.ProductResponse;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.entity.ProductUnit;
import com.example.RetailOS.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        return toResponse(create(request));
    }

    /**
     * Entity-returning create, shared with flows that add products inline
     * (e.g. recording a purchase for a not-yet-registered product).
     */
    @Transactional
    public Product create(CreateProductRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();

        String sku = normalizeSku(request.getSku());
        if (sku != null && productRepository.existsByShopIdAndSkuIgnoreCase(shopId, sku)) {
            throw new ResourceAlreadyExistsException(
                    "A product with this SKU already exists.");
        }

        UUID categoryId = resolveCategoryId(shopId, request.getCategoryId(), request.getCategoryName());

        Product product = Product.builder()
                .shopId(shopId)
                .categoryId(categoryId)
                .name(request.getName().trim())
                .sku(sku)
                .unit(request.getUnit() == null ? ProductUnit.PIECE : request.getUnit())
                .sellingPrice(request.getSellingPrice())
                .purchasePrice(request.getPurchasePrice() == null ? BigDecimal.ZERO : request.getPurchasePrice())
                .currentStock(request.getOpeningStock() == null ? BigDecimal.ZERO : request.getOpeningStock())
                .minStock(request.getMinStock() == null ? BigDecimal.ZERO : request.getMinStock())
                .build();

        return productRepository.save(product);
    }

    public ProductResponse getProduct(UUID id) {
        return toResponse(requireProduct(id));
    }

    /**
     * Both filters optional: search matches the name (case-insensitive,
     * contains); categoryId narrows to one category. Search wins if both
     * are given.
     */
    public List<ProductResponse> getProducts(String search, UUID categoryId) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository
                    .findAllByShopIdAndNameContainingIgnoreCaseOrderByNameAsc(shopId, search.trim());
        } else if (categoryId != null) {
            products = productRepository
                    .findAllByShopIdAndCategoryIdOrderByNameAsc(shopId, categoryId);
        } else {
            products = productRepository.findAllByShopIdOrderByNameAsc(shopId);
        }
        return toResponses(products);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        Product product = requireProduct(id);

        if (request.getSku() != null) {
            String sku = normalizeSku(request.getSku());
            if (sku != null
                    && productRepository.existsByShopIdAndSkuIgnoreCaseAndIdNot(shopId, sku, id)) {
                throw new ResourceAlreadyExistsException(
                        "A product with this SKU already exists.");
            }
            product.setSku(sku);
        }
        if (request.getName() != null) {
            product.setName(request.getName().trim());
        }
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        if (request.getSellingPrice() != null) {
            product.setSellingPrice(request.getSellingPrice());
        }
        if (request.getPurchasePrice() != null) {
            product.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getMinStock() != null) {
            product.setMinStock(request.getMinStock());
        }

        if (Boolean.TRUE.equals(request.getRemoveCategory())) {
            product.setCategoryId(null);
        } else if (request.getCategoryId() != null || request.getCategoryName() != null) {
            product.setCategoryId(
                    resolveCategoryId(shopId, request.getCategoryId(), request.getCategoryName()));
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(UUID id) {
        productRepository.delete(requireProduct(id));
    }

    // ---- Helpers ----

    private UUID resolveCategoryId(UUID shopId, UUID categoryId, String categoryName) {
        if (categoryId != null) {
            return categoryRepository.findByIdAndShopId(categoryId, shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found: " + categoryId))
                    .getId();
        }
        if (categoryName != null && !categoryName.isBlank()) {
            return categoryService.findOrCreateByName(shopId, categoryName).getId();
        }
        return null;
    }

    private String normalizeSku(String sku) {
        if (sku == null || sku.isBlank()) {
            return null;
        }
        return sku.trim();
    }

    private Product requireProduct(UUID id) {
        return productRepository.findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + id));
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategoryId() == null
                ? null
                : categoryRepository.findById(product.getCategoryId()).orElse(null);
        return ProductResponse.from(product, category);
    }

    private List<ProductResponse> toResponses(List<Product> products) {
        Map<UUID, Category> categoriesById = categoryRepository
                .findAllById(products.stream()
                        .map(Product::getCategoryId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        return products.stream()
                .map(p -> ProductResponse.from(p,
                        p.getCategoryId() == null ? null : categoriesById.get(p.getCategoryId())))
                .toList();
    }
}
