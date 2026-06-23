package com.worknear.api.professional.domain;

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
@Table(name = "professional_specializations")
public class ProfessionalSpecialization extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(nullable = false)
    private String label;
}
