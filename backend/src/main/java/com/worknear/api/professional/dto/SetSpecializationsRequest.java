package com.worknear.api.professional.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetSpecializationsRequest(
        @NotNull List<String> labels
) {}
