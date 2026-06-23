package com.worknear.api.review;

import com.worknear.api.booking.BookingRepository;
import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.domain.BookingStatus;
import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.ConflictException;
import com.worknear.api.common.exception.ForbiddenException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.professional.ProfessionalProfileRepository;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.review.domain.Review;
import com.worknear.api.review.dto.CreateReviewRequest;
import com.worknear.api.review.dto.ReviewResponse;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ProfessionalProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse create(UUID customerId, CreateReviewRequest req) {
        Booking booking = bookingRepository.findById(req.bookingId())
                .orElseThrow(() -> NotFoundException.of("Booking", req.bookingId()));
        if (!booking.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("You can only review your own bookings");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("You can review only completed bookings");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ConflictException("This booking has already been reviewed");
        }

        Review review = new Review();
        review.setBookingId(booking.getId());
        review.setCustomerId(customerId);
        review.setProfessionalId(booking.getProfessionalId());
        review.setRating(req.rating());
        review.setComment(req.comment());
        review = reviewRepository.save(review);

        updateAggregate(booking.getProfessionalId(), req.rating());

        String customerName = userRepository.findById(customerId).map(User::getFullName).orElse(null);
        return ReviewResponse.from(review, customerName);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> forProfessional(UUID professionalUserId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalUserId, pageable),
                r -> ReviewResponse.from(r, userRepository.findById(r.getCustomerId())
                        .map(User::getFullName).orElse(null)));
    }

    private void updateAggregate(UUID professionalUserId, int newRating) {
        if (professionalUserId == null) {
            return;
        }
        ProfessionalProfile profile = profileRepository.findByUserId(professionalUserId).orElse(null);
        if (profile == null) {
            return;
        }
        int count = profile.getRatingCount();
        BigDecimal currentTotal = profile.getRating().multiply(BigDecimal.valueOf(count));
        int newCount = count + 1;
        BigDecimal newAvg = currentTotal.add(BigDecimal.valueOf(newRating))
                .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);
        profile.setRating(newAvg);
        profile.setRatingCount(newCount);
    }
}
