package com.worknear.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationDecisionRequest(
        @NotNull Decision decision,
        @Size(max = 400) String note
) {
    public enum Decision {
        APPROVE,
        REJECT,
        MORE_INFO
    }
}
