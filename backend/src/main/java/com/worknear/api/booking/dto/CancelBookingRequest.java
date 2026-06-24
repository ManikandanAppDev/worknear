package com.worknear.api.booking.dto;

import com.worknear.api.booking.domain.CancellationReasonCode;
import jakarta.validation.constraints.Size;

public record CancelBookingRequest(
        CancellationReasonCode reasonCode,
        @Size(max = 500) String comment
) {}
