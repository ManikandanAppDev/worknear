package com.worknear.api.banner;

import com.worknear.api.banner.dto.BannerResponse;
import com.worknear.api.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public onboarding banner feed. Served pre-login (no auth) so the onboarding carousel can be
 * driven from the admin dashboard without an app release.
 */
@Tag(name = "Banners")
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "List active onboarding banners (optionally filtered by audience)")
    @GetMapping
    public ApiResponse<List<BannerResponse>> banners(
            @RequestParam(required = false, defaultValue = "ALL") String audience) {
        return ApiResponse.ok(bannerService.activeBanners(audience));
    }
}
