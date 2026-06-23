package com.worknear.api.professional.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "professional_availability")
public class ProfessionalAvailability extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    /** ISO day-of-week: 1=Monday .. 7=Sunday. */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean available = true;
}
