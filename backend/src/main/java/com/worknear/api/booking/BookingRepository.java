package com.worknear.api.booking;

import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.domain.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByCode(String code);

    Page<Booking> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatusIn(UUID customerId, Collection<BookingStatus> statuses, Pageable pageable);

    Page<Booking> findByProfessionalId(UUID professionalId, Pageable pageable);

    Page<Booking> findByProfessionalIdAndStatusIn(UUID professionalId, Collection<BookingStatus> statuses, Pageable pageable);

    long countByProfessionalIdAndStatus(UUID professionalId, BookingStatus status);

    long countByStatus(BookingStatus status);

    long countByCreatedAtAfter(Instant after);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("select coalesce(sum(b.amount), 0) from Booking b where b.status = com.worknear.api.booking.domain.BookingStatus.COMPLETED and b.completedAt >= :since")
    BigDecimal sumCompletedAmountSince(@Param("since") Instant since);

    @Query("select coalesce(sum(b.commission), 0) from Booking b where b.status = com.worknear.api.booking.domain.BookingStatus.COMPLETED and b.completedAt >= :since")
    BigDecimal sumCommissionSince(@Param("since") Instant since);
}
