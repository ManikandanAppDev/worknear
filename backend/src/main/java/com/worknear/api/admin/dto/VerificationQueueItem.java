package com.worknear.api.admin.dto;

import com.worknear.api.professional.domain.VerificationStatus;

import java.util.List;
import java.util.UUID;

public record VerificationQueueItem(
        UUID profileId,
        UUID userId,
        String name,
        String phone,
        List<String> services,
        int documentCount,
        VerificationStatus status
) {}
