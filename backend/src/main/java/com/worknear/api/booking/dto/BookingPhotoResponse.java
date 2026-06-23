package com.worknear.api.booking.dto;

import com.worknear.api.booking.domain.BookingPhoto;
import com.worknear.api.booking.domain.BookingPhotoType;

import java.util.UUID;

public record BookingPhotoResponse(
        UUID id,
        BookingPhotoType type,
        String fileUrl
) {
    public static BookingPhotoResponse from(BookingPhoto p) {
        return new BookingPhotoResponse(p.getId(), p.getType(), p.getFileUrl());
    }
}
