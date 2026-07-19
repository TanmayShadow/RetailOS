package com.example.RetailOS.category.service;

import com.example.RetailOS.category.dto.request.CreateCategoryRequest;
import com.example.RetailOS.category.dto.request.UpdateCategoryRequest;
import com.example.RetailOS.category.dto.response.CategoryResponse;
import com.example.RetailOS.category.entity.Category;
import com.example.RetailOS.category.repository.CategoryRepository;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public List<CategoryResponse> getCategories() {
        return categoryRepository
                .findAllByShopIdOrderByNameAsc(currentUserService.getCurrentShopId())
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        String name = request.getName().trim();

        if (categoryRepository.existsByShopIdAndNameIgnoreCase(shopId, name)) {
            throw new ResourceAlreadyExistsException(
                    "A category with this name already exists.");
        }

        Category category = Category.builder()
                .shopId(shopId)
                .name(name)
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        Category category = requireCategory(id, shopId);

        String name = request.getName().trim();
        if (categoryRepository.existsByShopIdAndNameIgnoreCaseAndIdNot(shopId, name, id)) {
            throw new ResourceAlreadyExistsException(
                    "A category with this name already exists.");
        }

        category.setName(name);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    /**
     * Products referencing this category keep existing — the DB FK is
     * ON DELETE SET NULL, so they simply become uncategorized.
     */
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = requireCategory(id, currentUserService.getCurrentShopId());
        categoryRepository.delete(category);
    }

    /**
     * Find-or-create by name (case-insensitive), used by the product flow
     * so cashiers can type a category without creating it first.
     */
    @Transactional
    public Category findOrCreateByName(UUID shopId, String name) {
        String trimmed = name.trim();
        return categoryRepository.findByShopIdAndNameIgnoreCase(shopId, trimmed)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .shopId(shopId)
                        .name(trimmed)
                        .build()));
    }

    private Category requireCategory(UUID id, UUID shopId) {
        return categoryRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found: " + id));
    }
}
