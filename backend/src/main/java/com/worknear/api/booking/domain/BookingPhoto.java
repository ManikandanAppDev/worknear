package com.worknear.api.booking.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "booking_photos")
public class BookingPhoto extends BaseEntity {

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingPhotoType type;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;
}
