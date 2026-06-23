package com.worknear.api.professional.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 2000) String bio,
        @Min(0) @Max(80) Integer experienceYears,
        @Min(1) @Max(100) Integer serviceRadiusKm,
        @Size(max = 80) String city,
        @Size(max = 120) String area,
        Double baseLatitude,
        Double baseLongitude,
        @Size(max = 200) String languages
) {}
