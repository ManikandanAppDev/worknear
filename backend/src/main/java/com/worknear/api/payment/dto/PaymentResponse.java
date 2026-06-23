package com.worknear.api.payment.dto;

import com.worknear.api.payment.domain.Payment;
import com.worknear.api.payment.domain.PaymentPurpose;
import com.worknear.api.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        PaymentPurpose purpose,
        String gatewayOrderId,
        String gatewayPaymentId
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getBookingId(), p.getAmount(), p.getCurrency(),
                p.getStatus(), p.getPurpose(), p.getGatewayOrderId(), p.getGatewayPaymentId());
    }
}
