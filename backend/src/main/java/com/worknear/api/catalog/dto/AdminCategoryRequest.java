package com.worknear.api.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Create / full-update payload for a service category from the admin dashboard. */
public record AdminCategoryRequest(
        @NotBlank @Size(max = 60) String slug,
        @NotBlank @Size(max = 80) String name,
        @Size(max = 40) String icon,
        @Size(max = 16) String color,
        Integer sortOrder,
        Boolean active,
        @NotNull @PositiveOrZero BigDecimal basePrice
) {}
