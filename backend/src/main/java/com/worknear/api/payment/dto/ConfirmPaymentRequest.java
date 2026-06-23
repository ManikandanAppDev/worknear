package com.worknear.api.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
        @NotBlank String gatewayOrderId,
        String gatewayPaymentId,
        String signature
) {}
