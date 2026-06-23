package com.worknear.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveDisputeRequest(
        @NotNull Resolution resolution,
        @Size(max = 1000) String note,
        /** Optional wallet refund to the customer when resolving in their favour. */
        java.math.BigDecimal refundAmount
) {
    public enum Resolution {
        RESOLVED,
        REJECTED
    }
}
