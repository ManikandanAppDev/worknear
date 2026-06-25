package com.worknear.api.catalog.dto;

import com.worknear.api.catalog.domain.ServiceCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String slug,
        String name,
        String icon,
        String color,
        BigDecimal basePrice
) {
    public static CategoryResponse from(ServiceCategory c) {
        return new CategoryResponse(c.getId(), c.getSlug(), c.getName(), c.getIcon(), c.getColor(), c.getBasePrice());
    }
}
