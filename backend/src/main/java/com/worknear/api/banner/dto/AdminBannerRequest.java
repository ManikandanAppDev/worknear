package com.worknear.api.banner.dto;

import jakarta.validation.constraints.Size;

public record AdminBannerRequest(
        @Size(max = 160) String title,
        @Size(max = 2000) String subtitle,
        @Size(max = 1024) String imageUrl,
        @Size(max = 80) String ctaLabel,
        /** CUSTOMER, PROFESSIONAL or ALL. Defaults to ALL when omitted. */
        String audience,
        Integer sortOrder,
        Boolean active
) {}
