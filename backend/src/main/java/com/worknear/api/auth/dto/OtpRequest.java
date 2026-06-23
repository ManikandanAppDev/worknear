package com.worknear.api.auth.dto;

import com.worknear.api.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpRequest(
        @NotBlank
        @Pattern(regexp = "\\+?[1-9]\\d{7,14}", message = "Invalid phone number (E.164 expected)")
        String phone,

        /** Role to assign if this phone is registering for the first time. Defaults to CUSTOMER. */
        Role role
) {}
