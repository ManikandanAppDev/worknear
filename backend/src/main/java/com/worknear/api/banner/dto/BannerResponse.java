package com.worknear.api.banner.dto;

import com.worknear.api.banner.domain.Banner;

import java.util.UUID;

public record BannerResponse(
        UUID id,
        String title,
        String subtitle,
        String imageUrl,
        String ctaLabel,
        String audience,
        int sortOrder,
        boolean active
) {
    public static BannerResponse from(Banner b) {
        return new BannerResponse(b.getId(), b.getTitle(), b.getSubtitle(), b.getImageUrl(),
                b.getCtaLabel(), b.getAudience(), b.getSortOrder(), b.isActive());
    }
}
