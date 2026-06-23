package com.worknear.api.professional.dto;

import com.worknear.api.professional.domain.VerificationStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProfessionalDetailResponse(
        UUID userId,
        UUID profileId,
        String name,
        String avatarUrl,
        String bio,
        BigDecimal rating,
        int ratingCount,
        int experienceYears,
        int serviceRadiusKm,
        String city,
        String area,
        String languages,
        boolean online,
        VerificationStatus verificationStatus,
        int jobsCompleted,
        List<String> specializations,
        List<ProfessionalServiceResponse> services,
        List<AvailabilityResponse> availability
) {}
