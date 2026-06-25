package com.worknear.api.catalog;

import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.catalog.dto.AdminCategoryRequest;
import com.worknear.api.catalog.dto.AdminCategoryResponse;
import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Admin-side management of the service category catalog and its reference prices. */
@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final ServiceCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> list() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc()
                .stream().map(AdminCategoryResponse::from).toList();
    }

    @Transactional
    public AdminCategoryResponse create(AdminCategoryRequest request) {
        String slug = normalizeSlug(request.slug());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("CATEGORY_SLUG_EXISTS",
                    "A category with slug '" + slug + "' already exists.");
        }
        ServiceCategory category = new ServiceCategory();
        category.setSlug(slug);
        apply(category, request);
        return AdminCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public AdminCategoryResponse update(UUID id, AdminCategoryRequest request) {
        ServiceCategory category = require(id);
        String slug = normalizeSlug(request.slug());
        if (!slug.equals(category.getSlug()) && categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("CATEGORY_SLUG_EXISTS",
                    "A category with slug '" + slug + "' already exists.");
        }
        category.setSlug(slug);
        apply(category, request);
        return AdminCategoryResponse.from(category);
    }

    @Transactional
    public AdminCategoryResponse updatePrice(UUID id, BigDecimal basePrice) {
        ServiceCategory category = require(id);
        category.setBasePrice(basePrice);
        return AdminCategoryResponse.from(category);
    }

    @Transactional
    public void deactivate(UUID id) {
        ServiceCategory category = require(id);
        category.setActive(false);
    }

    private void apply(ServiceCategory category, AdminCategoryRequest request) {
        category.setName(request.name().trim());
        category.setIcon(request.icon());
        category.setColor(request.color());
        if (request.sortOrder() != null) {
            category.setSortOrder(request.sortOrder());
        }
        if (request.active() != null) {
            category.setActive(request.active());
        }
        category.setBasePrice(request.basePrice());
    }

    private ServiceCategory require(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Category", id));
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }
}
