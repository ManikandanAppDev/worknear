package com.worknear.api.professional.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProfessionalServiceResponse(
        UUID categoryId,
        String categorySlug,
        String categoryName,
        BigDecimal basePrice,
        boolean active
) {}
