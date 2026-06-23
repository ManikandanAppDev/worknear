package com.worknear.api.auth.dto;

import com.worknear.api.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "\\+?[1-9]\\d{7,14}", message = "Invalid phone number (E.164 expected)")
        String phone,

        @NotBlank
        String code,

        /** Role assigned on first-time registration. Defaults to CUSTOMER. Ignored for existing users. */
        Role role
) {}
