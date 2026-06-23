package com.worknear.api.professional.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProfessionalSummaryResponse(
        UUID userId,
        UUID profileId,
        String name,
        String avatarUrl,
        BigDecimal rating,
        int ratingCount,
        int experienceYears,
        boolean verified,
        boolean online,
        BigDecimal price,
        Double distanceKm
) {}
