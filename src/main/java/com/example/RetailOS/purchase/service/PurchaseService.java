package com.example.RetailOS.purchase.service;

import com.example.RetailOS.platform.exception.BadRequestException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.product.dto.request.CreateProductRequest;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.repository.ProductRepository;
import com.example.RetailOS.product.service.ProductService;
import com.example.RetailOS.purchase.dto.request.CreatePurchaseRequest;
import com.example.RetailOS.purchase.dto.request.PurchaseItemRequest;
import com.example.RetailOS.purchase.dto.response.PurchaseItemResponse;
import com.example.RetailOS.purchase.dto.response.PurchaseResponse;
import com.example.RetailOS.purchase.entity.Purchase;
import com.example.RetailOS.purchase.entity.PurchaseItem;
import com.example.RetailOS.purchase.repository.PurchaseItemRepository;
import com.example.RetailOS.purchase.repository.PurchaseRepository;
import com.example.RetailOS.supplier.entity.Supplier;
import com.example.RetailOS.supplier.repository.SupplierRepository;
import com.example.RetailOS.supplier.service.SupplierService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;
    private final CurrentUserService currentUserService;

    /**
     * Records a purchase and increases each product's stock in the same
     * transaction. Subtotals and the total are always computed server-side.
     */
    @Transactional
    public PurchaseResponse createPurchase(CreatePurchaseRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        UUID supplierId = resolveSupplierId(shopId, request.getSupplierId(), request.getSupplierName());

        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .shopId(shopId)
                .supplierId(supplierId)
                .purchaseDate(request.getPurchaseDate() == null
                        ? LocalDate.now() : request.getPurchaseDate())
                .build());

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();
        for (PurchaseItemRequest itemRequest : request.getItems()) {

            Product product = resolveProduct(shopId, itemRequest);
            requireWholeQuantityForCountUnits(product, itemRequest.getQuantity());

            BigDecimal subtotal = itemRequest.getQuantity()
                    .multiply(itemRequest.getPurchasePrice())
                    .setScale(2, RoundingMode.HALF_UP);

            items.add(PurchaseItem.builder()
                    .purchaseId(purchase.getId())
                    .productId(product.getId())
                    .quantity(itemRequest.getQuantity())
                    .purchasePrice(itemRequest.getPurchasePrice())
                    .subtotal(subtotal)
                    .build());

            product.setCurrentStock(product.getCurrentStock().add(itemRequest.getQuantity()));
            productRepository.save(product);

            total = total.add(subtotal);
        }
        purchaseItemRepository.saveAll(items);

        purchase.setTotalAmount(total);
        purchase = purchaseRepository.save(purchase);

        return toDetailResponse(purchase, items);
    }

    public List<PurchaseResponse> getPurchases(UUID supplierId) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Purchase> purchases = supplierId == null
                ? purchaseRepository.findAllByShopIdOrderByPurchaseDateDescCreatedAtDesc(shopId)
                : purchaseRepository.findAllByShopIdAndSupplierIdOrderByPurchaseDateDescCreatedAtDesc(
                        shopId, supplierId);

        Map<UUID, String> supplierNames = supplierNamesById(purchases);
        return purchases.stream()
                .map(p -> PurchaseResponse.from(p, supplierNames.get(p.getSupplierId()), null))
                .toList();
    }

    public PurchaseResponse getPurchase(UUID id) {
        Purchase purchase = requirePurchase(id);
        return toDetailResponse(purchase, purchaseItemRepository.findAllByPurchaseId(id));
    }

    /**
     * Removes a wrongly recorded purchase and reverses its stock effect
     * in the same transaction.
     */
    @Transactional
    public void deletePurchase(UUID id) {

        Purchase purchase = requirePurchase(id);

        for (PurchaseItem item : purchaseItemRepository.findAllByPurchaseId(id)) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setCurrentStock(product.getCurrentStock().subtract(item.getQuantity()));
                productRepository.save(product);
            });
        }

        purchaseItemRepository.deleteAllByPurchaseId(id);
        purchaseRepository.delete(purchase);
    }

    // ---- Helpers ----

    /**
     * Existing product (productId, optionally repricing it) or inline
     * creation (productName + sellingPrice) — see PurchaseItemRequest.
     */
    private Product resolveProduct(UUID shopId, PurchaseItemRequest itemRequest) {

        if (itemRequest.getProductId() != null) {
            Product product = productRepository
                    .findByIdAndShopId(itemRequest.getProductId(), shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemRequest.getProductId()));
            if (itemRequest.getSellingPrice() != null) {
                product.setSellingPrice(itemRequest.getSellingPrice());
            }
            return product;
        }

        if (itemRequest.getProductName() != null && !itemRequest.getProductName().isBlank()) {
            if (itemRequest.getSellingPrice() == null) {
                throw new BadRequestException(
                        "sellingPrice is required when adding a new product from a purchase.");
            }
            CreateProductRequest createRequest = new CreateProductRequest();
            createRequest.setName(itemRequest.getProductName());
            createRequest.setSku(itemRequest.getSku());
            createRequest.setUnit(itemRequest.getUnit());
            createRequest.setSellingPrice(itemRequest.getSellingPrice());
            // New products created inline from a purchase start out priced
            // at what was just paid for them.
            createRequest.setPurchasePrice(itemRequest.getPurchasePrice());
            createRequest.setCategoryId(itemRequest.getCategoryId());
            createRequest.setCategoryName(itemRequest.getCategoryName());
            return productService.create(createRequest);
        }

        throw new BadRequestException(
                "Each purchase item needs either a productId or a productName.");
    }

    private void requireWholeQuantityForCountUnits(Product product, BigDecimal quantity) {
        if (!product.getUnit().isFractionalAllowed()
                && quantity.stripTrailingZeros().scale() > 0) {
            throw new BadRequestException(
                    product.getName() + " is sold per " + product.getUnit()
                            + " — quantity must be a whole number.");
        }
    }

    private UUID resolveSupplierId(UUID shopId, UUID supplierId, String supplierName) {
        if (supplierId != null) {
            return supplierRepository.findByIdAndShopId(supplierId, shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found: " + supplierId))
                    .getId();
        }
        if (supplierName != null && !supplierName.isBlank()) {
            return supplierService.findOrCreateByName(shopId, supplierName).getId();
        }
        return null;
    }

    private Purchase requirePurchase(UUID id) {
        return purchaseRepository.findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase not found: " + id));
    }

    private PurchaseResponse toDetailResponse(Purchase purchase, List<PurchaseItem> items) {

        Map<UUID, Product> productsById = productRepository
                .findAllById(items.stream().map(PurchaseItem::getProductId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        String supplierName = purchase.getSupplierId() == null
                ? null
                : supplierRepository.findById(purchase.getSupplierId())
                        .map(Supplier::getName).orElse(null);

        return PurchaseResponse.from(purchase, supplierName,
                items.stream()
                        .map(i -> PurchaseItemResponse.from(i, productsById.get(i.getProductId())))
                        .toList());
    }

    private Map<UUID, String> supplierNamesById(List<Purchase> purchases) {
        return supplierRepository
                .findAllById(purchases.stream()
                        .map(Purchase::getSupplierId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName));
    }
}
