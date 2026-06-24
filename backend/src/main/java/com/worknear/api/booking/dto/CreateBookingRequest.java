package com.worknear.api.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.worknear.api.payment.domain.PaymentMethod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID professionalId,
        @NotNull UUID categoryId,

        @NotNull @FutureOrPresent LocalDate scheduledDate,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime slotStart,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime slotEnd,

        /** Optional saved address id; if absent, the inline address fields are used. */
        UUID addressId,
        @Size(max = 300) String addressLine,
        @Size(max = 80) String city,
        Double latitude,
        Double longitude,

        @Size(max = 2000) String problemDescription,
        PaymentMethod paymentMethod
) {}
