package com.example.RetailOS.supplier.service;

import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.supplier.dto.request.CreateSupplierRequest;
import com.example.RetailOS.supplier.dto.request.UpdateSupplierRequest;
import com.example.RetailOS.supplier.dto.response.SupplierResponse;
import com.example.RetailOS.supplier.entity.Supplier;
import com.example.RetailOS.supplier.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CurrentUserService currentUserService;

    public List<SupplierResponse> getSuppliers() {
        return supplierRepository
                .findAllByShopIdOrderByNameAsc(currentUserService.getCurrentShopId())
                .stream()
                .map(SupplierResponse::from)
                .toList();
    }

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        String name = request.getName().trim();

        if (supplierRepository.existsByShopIdAndNameIgnoreCase(shopId, name)) {
            throw new ResourceAlreadyExistsException(
                    "A supplier with this name already exists.");
        }

        Supplier supplier = Supplier.builder()
                .shopId(shopId)
                .name(name)
                .phone(request.getPhone())
                .build();

        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(UUID id, UpdateSupplierRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        Supplier supplier = requireSupplier(id, shopId);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (supplierRepository.existsByShopIdAndNameIgnoreCaseAndIdNot(shopId, name, id)) {
                throw new ResourceAlreadyExistsException(
                        "A supplier with this name already exists.");
            }
            supplier.setName(name);
        }
        if (request.getPhone() != null) {
            supplier.setPhone(request.getPhone().trim());
        }

        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    /**
     * Past purchases keep existing — the DB FK is ON DELETE SET NULL,
     * so they simply lose the supplier reference.
     */
    @Transactional
    public void deleteSupplier(UUID id) {
        Supplier supplier = requireSupplier(id, currentUserService.getCurrentShopId());
        supplierRepository.delete(supplier);
    }

    /**
     * Find-or-create by name (case-insensitive), used by the purchase flow
     * so a supplier can be typed inline while recording a purchase.
     */
    @Transactional
    public Supplier findOrCreateByName(UUID shopId, String name) {
        String trimmed = name.trim();
        return supplierRepository.findByShopIdAndNameIgnoreCase(shopId, trimmed)
                .orElseGet(() -> supplierRepository.save(Supplier.builder()
                        .shopId(shopId)
                        .name(trimmed)
                        .build()));
    }

    private Supplier requireSupplier(UUID id, UUID shopId) {
        return supplierRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found: " + id));
    }
}
