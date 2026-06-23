package com.worknear.api.professional.dto;

import com.worknear.api.professional.domain.BankMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BankAccountRequest(
        @NotNull BankMethod method,
        @Size(max = 120) String upiId,
        @Size(max = 120) String accountName,
        @Size(max = 40) String accountNumber,
        @Size(max = 20) String ifsc
) {}
