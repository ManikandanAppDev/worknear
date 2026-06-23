package com.worknear.api.professional.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SetServicesRequest(
        @NotEmpty @Valid List<ServiceItem> services
) {
    public record ServiceItem(
            @NotNull UUID categoryId,
            @NotNull @PositiveOrZero BigDecimal basePrice
    ) {}
}
