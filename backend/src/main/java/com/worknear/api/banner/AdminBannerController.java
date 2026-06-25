package com.worknear.api.banner;

import com.worknear.api.banner.dto.AdminBannerRequest;
import com.worknear.api.banner.dto.BannerResponse;
import com.worknear.api.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin dashboard endpoints for managing onboarding banners.
 * Secured by the {@code /api/v1/admin/**} -> ROLE_ADMIN rule in SecurityConfig.
 */
@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @Operation(summary = "List all banners (including inactive)")
    @GetMapping
    public ApiResponse<List<BannerResponse>> list() {
        return ApiResponse.ok(bannerService.listAll());
    }

    @Operation(summary = "Create a banner")
    @PostMapping
    public ApiResponse<BannerResponse> create(@Valid @RequestBody AdminBannerRequest request) {
        return ApiResponse.ok(bannerService.create(request));
    }

    @Operation(summary = "Update a banner")
    @PutMapping("/{id}")
    public ApiResponse<BannerResponse> update(@PathVariable UUID id,
                                              @Valid @RequestBody AdminBannerRequest request) {
        return ApiResponse.ok(bannerService.update(id, request));
    }

    @Operation(summary = "Delete a banner")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        bannerService.delete(id);
        return ApiResponse.message("Banner deleted");
    }
}
