package com.worknear.api.catalog.dto;

import com.worknear.api.catalog.domain.ServiceCategory;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String slug,
        String name,
        String icon,
        String color
) {
    public static CategoryResponse from(ServiceCategory c) {
        return new CategoryResponse(c.getId(), c.getSlug(), c.getName(), c.getIcon(), c.getColor());
    }
}
