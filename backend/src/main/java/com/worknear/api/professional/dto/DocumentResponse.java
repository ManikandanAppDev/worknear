package com.worknear.api.professional.dto;

import com.worknear.api.professional.domain.DocumentStatus;
import com.worknear.api.professional.domain.DocumentType;
import com.worknear.api.professional.domain.ProfessionalDocument;

import java.util.UUID;

public record DocumentResponse(
        UUID id,
        DocumentType type,
        String fileUrl,
        String originalName,
        DocumentStatus status,
        String reviewNote
) {
    public static DocumentResponse from(ProfessionalDocument d) {
        return new DocumentResponse(d.getId(), d.getType(), d.getFileUrl(), d.getOriginalName(),
                d.getStatus(), d.getReviewNote());
    }
}
