package com.worknear.api.review.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(nullable = false)
    private int rating;

    private String comment;
}
