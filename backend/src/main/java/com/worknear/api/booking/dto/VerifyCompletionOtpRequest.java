package com.worknear.api.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyCompletionOtpRequest(
        @NotBlank String otp
) {}
