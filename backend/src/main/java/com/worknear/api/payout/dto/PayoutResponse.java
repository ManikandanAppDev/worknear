package com.worknear.api.payout.dto;

import com.worknear.api.payout.domain.Payout;
import com.worknear.api.payout.domain.PayoutStatus;
import com.worknear.api.professional.domain.BankMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayoutResponse(
        UUID id,
        UUID professionalId,
        BigDecimal amount,
        BankMethod method,
        String destination,
        PayoutStatus status,
        String failureReason,
        Instant processedAt,
        Instant createdAt
) {
    public static PayoutResponse from(Payout p) {
        return new PayoutResponse(p.getId(), p.getProfessionalId(), p.getAmount(), p.getMethod(),
                p.getDestination(), p.getStatus(), p.getFailureReason(), p.getProcessedAt(), p.getCreatedAt());
    }
}
