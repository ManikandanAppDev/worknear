package com.worknear.api.booking.dto;

import jakarta.validation.constraints.Size;

/** Optional note when a professional rejects a booking request. */
public record BookingNoteRequest(
        @Size(max = 300) String note
) {}
