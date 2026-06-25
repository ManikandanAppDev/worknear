package com.worknear.api.catalog;

import com.worknear.api.catalog.dto.AdminCategoryPriceRequest;
import com.worknear.api.catalog.dto.AdminCategoryRequest;
import com.worknear.api.catalog.dto.AdminCategoryResponse;
import com.worknear.api.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin dashboard endpoints for managing the service category catalog and its prices.
 * Secured by the {@code /api/v1/admin/**} -> ROLE_ADMIN rule in SecurityConfig.
 */
@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    @Operation(summary = "List all categories (including inactive)")
    @GetMapping
    public ApiResponse<List<AdminCategoryResponse>> list() {
        return ApiResponse.ok(adminCatalogService.list());
    }

    @Operation(summary = "Create a category")
    @PostMapping
    public ApiResponse<AdminCategoryResponse> create(@Valid @RequestBody AdminCategoryRequest request) {
        return ApiResponse.ok(adminCatalogService.create(request));
    }

    @Operation(summary = "Update a category")
    @PutMapping("/{id}")
    public ApiResponse<AdminCategoryResponse> update(@PathVariable UUID id,
                                                     @Valid @RequestBody AdminCategoryRequest request) {
        return ApiResponse.ok(adminCatalogService.update(id, request));
    }

    @Operation(summary = "Update only a category's reference price")
    @PatchMapping("/{id}/price")
    public ApiResponse<AdminCategoryResponse> updatePrice(@PathVariable UUID id,
                                                          @Valid @RequestBody AdminCategoryPriceRequest request) {
        return ApiResponse.ok(adminCatalogService.updatePrice(id, request.basePrice()));
    }

    @Operation(summary = "Deactivate (soft-delete) a category")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        adminCatalogService.deactivate(id);
        return ApiResponse.message("Category deactivated");
    }
}
