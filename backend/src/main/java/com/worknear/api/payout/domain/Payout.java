package com.worknear.api.payout.domain;

import com.worknear.api.common.domain.BaseEntity;
import com.worknear.api.professional.domain.BankMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payouts")
public class Payout extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankMethod method;

    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status = PayoutStatus.REQUESTED;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "processed_at")
    private Instant processedAt;
}
