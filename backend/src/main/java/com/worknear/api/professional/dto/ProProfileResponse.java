package com.worknear.api.professional.dto;

import com.worknear.api.professional.domain.VerificationStatus;

import java.util.List;
import java.util.UUID;

/**
 * The professional's own (private) view of their profile, including verification,
 * documents and payout details.
 */
public record ProProfileResponse(
        UUID userId,
        UUID profileId,
        String name,
        String avatarUrl,
        String bio,
        int experienceYears,
        int serviceRadiusKm,
        String city,
        String area,
        Double baseLatitude,
        Double baseLongitude,
        String languages,
        boolean online,
        VerificationStatus verificationStatus,
        java.math.BigDecimal rating,
        int ratingCount,
        int jobsCompleted,
        List<String> specializations,
        List<ProfessionalServiceResponse> services,
        List<AvailabilityResponse> availability,
        List<DocumentResponse> documents,
        BankAccountResponse bankAccount
) {}
