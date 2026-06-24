package com.worknear.api.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Payload to move an existing booking to a new date/time slot. */
public record RescheduleBookingRequest(
        @NotNull @FutureOrPresent LocalDate scheduledDate,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime slotStart,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime slotEnd
) {}
