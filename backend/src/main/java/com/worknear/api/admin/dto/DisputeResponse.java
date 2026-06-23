package com.worknear.api.admin.dto;

import com.worknear.api.dispute.domain.Dispute;
import com.worknear.api.dispute.domain.DisputeStatus;

import java.time.Instant;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID bookingId,
        UUID raisedBy,
        String reason,
        String description,
        DisputeStatus status,
        String resolutionNote,
        Instant createdAt
) {
    public static DisputeResponse from(Dispute d) {
        return new DisputeResponse(d.getId(), d.getBookingId(), d.getRaisedBy(), d.getReason(),
                d.getDescription(), d.getStatus(), d.getResolutionNote(), d.getCreatedAt());
    }
}
