package com.worknear.api.chat.dto;

import com.worknear.api.chat.domain.ChatThread;

import java.time.Instant;
import java.util.UUID;

public record ChatThreadResponse(
        UUID id,
        UUID bookingId,
        UUID customerId,
        UUID professionalId,
        String counterpartName,
        Instant lastMessageAt
) {
    public static ChatThreadResponse from(ChatThread t, String counterpartName) {
        return new ChatThreadResponse(t.getId(), t.getBookingId(), t.getCustomerId(),
                t.getProfessionalId(), counterpartName, t.getLastMessageAt());
    }
}
