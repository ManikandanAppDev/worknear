package com.worknear.api.dispute.domain;

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
@Table(name = "disputes")
public class Dispute extends BaseEntity {

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "raised_by", nullable = false)
    private UUID raisedBy;

    @Column(nullable = false)
    private String reason;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(name = "resolution_note")
    private String resolutionNote;

    @Column(name = "resolved_by")
    private UUID resolvedBy;
}
