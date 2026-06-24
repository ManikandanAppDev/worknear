package com.worknear.api.booking;

import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.dto.BookingPhotoResponse;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ServiceCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BookingPhotoRepository photoRepository;

    public BookingResponse toResponse(Booking b) {
        String categoryName = categoryRepository.findById(b.getCategoryId())
                .map(ServiceCategory::getName).orElse(null);
        String customerName = nameOf(b.getCustomerId());
        String proName = b.getProfessionalId() == null ? null : nameOf(b.getProfessionalId());
        List<BookingPhotoResponse> photos = photoRepository.findByBookingId(b.getId()).stream()
                .map(BookingPhotoResponse::from).toList();

        return new BookingResponse(
                b.getId(), b.getCode(), b.getCustomerId(), b.getProfessionalId(), b.getCategoryId(),
                categoryName, customerName, proName,
                b.getScheduledDate(), b.getSlotStart(), b.getSlotEnd(),
                b.getAddressLine(), b.getCity(), b.getLatitude(), b.getLongitude(),
                b.getProblemDescription(), b.getStatus(), b.getAmount(), b.getCommission(), b.getProEarning(),
                b.getPaymentMethod(), b.getConfirmedAt(), b.getCompletedAt(), b.getCreatedAt(), photos,
                b.getRescheduleCount(), RESCHEDULE_MAX,
                canReschedule(b), canCancel(b));
    }

    private static final int RESCHEDULE_MAX = 2;

    private static boolean canReschedule(Booking b) {
        return (b.getStatus() == com.worknear.api.booking.domain.BookingStatus.PENDING
                || b.getStatus() == com.worknear.api.booking.domain.BookingStatus.CONFIRMED)
                && b.getRescheduleCount() < RESCHEDULE_MAX;
    }

    private static boolean canCancel(Booking b) {
        return b.getStatus().canTransitionTo(com.worknear.api.booking.domain.BookingStatus.CANCELLED);
    }

    private String nameOf(UUID userId) {
        return userRepository.findById(userId).map(User::getFullName).orElse(null);
    }
}
