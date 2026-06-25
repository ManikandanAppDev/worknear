package com.worknear.api.booking;

import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.dto.BookingPhotoResponse;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.Role;
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

    public BookingResponse toResponse(Booking b, UUID viewerId) {
        String categoryName = categoryRepository.findById(b.getCategoryId())
                .map(ServiceCategory::getName).orElse(null);
        String customerName = nameOf(b.getCustomerId());
        String proName = b.getProfessionalId() == null ? null : nameOf(b.getProfessionalId());
        List<BookingPhotoResponse> photos = photoRepository.findByBookingId(b.getId()).stream()
                .map(BookingPhotoResponse::from).toList();
        String completionOtp = b.getCustomerId().equals(viewerId)
                && b.getStatus() == com.worknear.api.booking.domain.BookingStatus.COMPLETED_PENDING_OTP
                ? b.getCompletionOtpCode()
                : null;

        User viewer = userRepository.findById(viewerId).orElse(null);
        boolean proViewer = viewer != null
                && viewer.getRole() == Role.PROFESSIONAL
                && b.getProfessionalId() != null
                && b.getProfessionalId().equals(viewerId);
        boolean customerContactVisible = !proViewer || b.getStatus() != com.worknear.api.booking.domain.BookingStatus.PENDING;

        String customerPhone = null;
        if (customerContactVisible) {
            customerPhone = userRepository.findById(b.getCustomerId()).map(User::getPhone).orElse(null);
        }

        String addressLine = customerContactVisible ? b.getAddressLine() : null;
        String city = customerContactVisible ? b.getCity() : null;
        Double latitude = customerContactVisible ? b.getLatitude() : null;
        Double longitude = customerContactVisible ? b.getLongitude() : null;

        boolean customerViewer = viewer != null && b.getCustomerId().equals(viewerId);
        boolean proContactVisible = customerViewer
                && b.getStatus() != com.worknear.api.booking.domain.BookingStatus.PENDING
                && b.getStatus() != com.worknear.api.booking.domain.BookingStatus.REJECTED
                && b.getStatus() != com.worknear.api.booking.domain.BookingStatus.CANCELLED;
        String professionalPhone = null;
        if (proContactVisible && b.getProfessionalId() != null) {
            professionalPhone = userRepository.findById(b.getProfessionalId()).map(User::getPhone).orElse(null);
        }

        return new BookingResponse(
                b.getId(), b.getCode(), b.getCustomerId(), b.getProfessionalId(), b.getCategoryId(),
                categoryName, customerName, customerPhone, proName, professionalPhone,
                b.getScheduledDate(), b.getSlotStart(), b.getSlotEnd(),
                addressLine, city, latitude, longitude,
                b.getProblemDescription(), b.getStatus(), displayStatus(b), b.getAmount(), b.getCommission(),
                b.getProEarning(), b.getLockedAmount(), b.getPaymentMethod(), b.getConfirmedAt(),
                b.getOnTheWayAt(), b.getArrivedAt(), b.getWorkStartedAt(), b.getWorkCompletedAt(),
                b.getCompletedAt(), b.getPaymentReleasedAt(), completionOtp, b.getCompletionOtpExpiresAt(),
                b.getCreatedAt(), photos,
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

    private static String displayStatus(Booking b) {
        return switch (b.getStatus()) {
            case PENDING -> "Assigned";
            case CONFIRMED -> "Confirmed";
            case ON_THE_WAY -> "On the way";
            case ARRIVED -> "Arrived";
            case IN_PROGRESS -> "Work started";
            case COMPLETED_PENDING_OTP -> "Completion OTP pending";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
            case REJECTED -> "Rejected";
            case NO_SHOW -> "Professional no-show";
        };
    }

    private String nameOf(UUID userId) {
        return userRepository.findById(userId).map(User::getFullName).orElse(null);
    }
}
