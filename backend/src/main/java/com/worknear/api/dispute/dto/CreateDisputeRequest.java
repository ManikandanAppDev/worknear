package com.worknear.api.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDisputeRequest(
        @NotNull UUID bookingId,
        @NotBlank @Size(max = 120) String reason,
        @Size(max = 1000) String description
) {}
