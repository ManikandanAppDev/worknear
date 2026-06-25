package com.worknear.api.auth.dto;

import com.worknear.api.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "\\+91[6-9]\\d{9}", message = "Enter a valid 10-digit mobile number (+91XXXXXXXXXX)")
        String phone,

        @NotBlank
        String code,

        /** Role assigned on first-time registration. Defaults to CUSTOMER. Ignored for existing users. */
        Role role
) {}
