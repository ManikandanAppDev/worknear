package com.worknear.api.payment.dto;

import com.worknear.api.payment.domain.PaymentMethod;
import com.worknear.api.payment.domain.PaymentPurpose;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull PaymentPurpose purpose,
        /** Required when purpose=BOOKING. */
        UUID bookingId,
        /** Required when purpose=WALLET_TOPUP. */
        BigDecimal amount,
        PaymentMethod method
) {}
