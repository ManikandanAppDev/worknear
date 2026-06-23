package com.worknear.api.professional.dto;

import java.time.LocalTime;

public record AvailabilityResponse(
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean available
) {}
