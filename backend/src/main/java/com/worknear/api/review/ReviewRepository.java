package com.worknear.api.review;

import com.worknear.api.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByProfessionalIdOrderByCreatedAtDesc(UUID professionalId, Pageable pageable);

    boolean existsByBookingId(UUID bookingId);
}
