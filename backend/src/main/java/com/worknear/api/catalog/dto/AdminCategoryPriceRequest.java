package com.worknear.api.catalog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Lightweight payload for updating just a category's reference price. */
public record AdminCategoryPriceRequest(
        @NotNull @PositiveOrZero BigDecimal basePrice
) {}
