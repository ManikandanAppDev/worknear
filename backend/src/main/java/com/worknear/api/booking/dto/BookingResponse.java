package com.worknear.api.booking.dto;

import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.domain.BookingStatus;
import com.worknear.api.payment.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String code,
        UUID customerId,
        UUID professionalId,
        UUID categoryId,
        String categoryName,
        String customerName,
        String professionalName,
        LocalDate scheduledDate,
        LocalTime slotStart,
        LocalTime slotEnd,
        String addressLine,
        String city,
        Double latitude,
        Double longitude,
        String problemDescription,
        BookingStatus status,
        BigDecimal amount,
        BigDecimal commission,
        BigDecimal proEarning,
        PaymentMethod paymentMethod,
        Instant confirmedAt,
        Instant completedAt,
        Instant createdAt,
        List<BookingPhotoResponse> photos,
        int rescheduleCount,
        int rescheduleMax,
        boolean canReschedule,
        boolean canCancel
) {}
