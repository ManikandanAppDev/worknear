package com.worknear.api.professional.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record SetAvailabilityRequest(
        @Valid List<Slot> slots
) {
    public record Slot(
            @Min(1) @Max(7) int dayOfWeek,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime startTime,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime endTime
    ) {}
}
