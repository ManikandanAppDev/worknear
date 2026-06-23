package com.worknear.api.booking;

import com.worknear.api.booking.domain.BookingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingPhotoRepository extends JpaRepository<BookingPhoto, UUID> {
    List<BookingPhoto> findByBookingId(UUID bookingId);
}
