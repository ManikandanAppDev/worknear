package com.worknear.api.payout.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PayoutRequestDto(
        @NotNull @Positive BigDecimal amount
) {}
