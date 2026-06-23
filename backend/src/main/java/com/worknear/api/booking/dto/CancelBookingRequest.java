package com.worknear.api.booking.dto;

import jakarta.validation.constraints.Size;

public record CancelBookingRequest(
        @Size(max = 300) String reason
) {}
