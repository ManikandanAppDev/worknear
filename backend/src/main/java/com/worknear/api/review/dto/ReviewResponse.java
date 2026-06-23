package com.worknear.api.review.dto;

import com.worknear.api.review.domain.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        UUID customerId,
        String customerName,
        UUID professionalId,
        int rating,
        String comment,
        Instant createdAt
) {
    public static ReviewResponse from(Review r, String customerName) {
        return new ReviewResponse(r.getId(), r.getBookingId(), r.getCustomerId(), customerName,
                r.getProfessionalId(), r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
