package com.worknear.api.professional;

import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.professional.dto.ProfessionalDetailResponse;
import com.worknear.api.professional.dto.ProfessionalSort;
import com.worknear.api.professional.dto.ProfessionalSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Professionals (Directory)")
@RestController
@RequestMapping("/api/v1/professionals")
@RequiredArgsConstructor
public class ProfessionalDirectoryController {

    private final ProfessionalDirectoryService directoryService;
    private final ServiceCategoryRepository categoryRepository;

    @Operation(summary = "Search professionals by category, with optional location-based sorting")
    @GetMapping
    public ApiResponse<List<ProfessionalSummaryResponse>> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false, defaultValue = "RECOMMENDED") ProfessionalSort sort,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        UUID resolvedCategory = resolveCategory(categoryId, categorySlug);
        return ApiResponse.ok(directoryService.search(resolvedCategory, sort, lat, lng, limit));
    }

    @Operation(summary = "Get a professional's public profile")
    @GetMapping("/{userId}")
    public ApiResponse<ProfessionalDetailResponse> detail(@PathVariable UUID userId) {
        return ApiResponse.ok(directoryService.getDetailByUserId(userId));
    }

    private UUID resolveCategory(UUID categoryId, String categorySlug) {
        if (categoryId != null) {
            return categoryId;
        }
        if (categorySlug != null) {
            return categoryRepository.findBySlug(categorySlug)
                    .map(ServiceCategory::getId)
                    .orElseThrow(() -> NotFoundException.of("Category", categorySlug));
        }
        throw new com.worknear.api.common.exception.BadRequestException("categoryId or categorySlug is required");
    }
}
