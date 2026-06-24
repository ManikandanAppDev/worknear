package com.worknear.api.booking.domain;

import com.worknear.api.common.domain.BaseEntity;
import com.worknear.api.payment.domain.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "professional_id")
    private UUID professionalId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "slot_start", nullable = false)
    private LocalTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private LocalTime slotEnd;

    @Column(name = "address_line")
    private String addressLine;

    private String city;
    private Double latitude;
    private Double longitude;

    @Column(name = "problem_description", columnDefinition = "text")
    private String problemDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "pro_earning", nullable = false)
    private BigDecimal proEarning = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancellation_comment", length = 500)
    private String cancellationComment;

    @Column(name = "late_cancel", nullable = false)
    private boolean lateCancel = false;

    @Column(name = "cancellation_fee")
    private BigDecimal cancellationFee;

    @Column(name = "reschedule_count", nullable = false)
    private int rescheduleCount = 0;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
