package com.worknear.api.payment.dto;

import com.worknear.api.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID paymentId,
        String gatewayOrderId,
        String gatewayKey,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        boolean mock
) {}
