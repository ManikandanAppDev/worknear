package com.worknear.api.professional.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "professional_profiles")
public class ProfessionalProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "experience_years", nullable = false)
    private int experienceYears = 0;

    @Column(name = "service_radius_km", nullable = false)
    private int serviceRadiusKm = 8;

    private String city;
    private String area;

    @Column(name = "base_latitude")
    private Double baseLatitude;

    @Column(name = "base_longitude")
    private Double baseLongitude;

    private String languages;

    @Column(nullable = false)
    private boolean online = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNSUBMITTED;

    @Column(nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount = 0;

    @Column(name = "jobs_completed", nullable = false)
    private int jobsCompleted = 0;
}
