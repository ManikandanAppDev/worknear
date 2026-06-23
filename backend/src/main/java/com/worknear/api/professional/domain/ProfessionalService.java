package com.worknear.api.professional.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "professional_services")
public class ProfessionalService extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}
