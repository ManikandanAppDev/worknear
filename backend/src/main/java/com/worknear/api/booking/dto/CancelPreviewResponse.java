package com.worknear.api.booking.dto;

import java.math.BigDecimal;

/** Server-computed cancellation quote shown before the customer confirms. */
public record CancelPreviewResponse(
        boolean canCancel,
        boolean feeApplies,
        BigDecimal feeAmount,
        String feeReason,
        boolean lateCancel,
        double hoursUntilSlot,
        int freeLateCancelsUsedThisMonth,
        int freeLateCancelsLimit,
        int freeLateCancelsRemaining,
        BigDecimal walletBalance,
        boolean sufficientWalletBalance,
        String message
) {}
