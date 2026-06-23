package com.worknear.api.catalog;

import com.worknear.api.catalog.dto.CategoryResponse;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalog")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CatalogController {

    private final ServiceCategoryRepository categoryRepository;

    @Operation(summary = "List active service categories")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream().map(CategoryResponse::from).toList());
    }

    @Operation(summary = "Get a category by slug")
    @GetMapping("/{slug}")
    public ApiResponse<CategoryResponse> category(@PathVariable String slug) {
        return ApiResponse.ok(categoryRepository.findBySlug(slug)
                .map(CategoryResponse::from)
                .orElseThrow(() -> NotFoundException.of("Category", slug)));
    }
}
