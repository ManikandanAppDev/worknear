package com.worknear.api.catalog.dto;

import com.worknear.api.catalog.domain.ServiceCategory;

import java.math.BigDecimal;
import java.util.UUID;

/** Admin view of a category — includes inactive categories, sort order and price. */
public record AdminCategoryResponse(
        UUID id,
        String slug,
        String name,
        String icon,
        String color,
        int sortOrder,
        boolean active,
        BigDecimal basePrice
) {
    public static AdminCategoryResponse from(ServiceCategory c) {
        return new AdminCategoryResponse(
                c.getId(), c.getSlug(), c.getName(), c.getIcon(), c.getColor(),
                c.getSortOrder(), c.isActive(), c.getBasePrice());
    }
}
