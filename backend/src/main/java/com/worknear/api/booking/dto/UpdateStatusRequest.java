package com.worknear.api.booking.dto;

import com.worknear.api.booking.domain.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
        @NotNull BookingStatus status,
        @Size(max = 300) String note
) {}
