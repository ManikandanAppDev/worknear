package com.worknear.api.auth.dto;

import com.worknear.api.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpRequest(
        @NotBlank
        @Pattern(regexp = "\\+91[6-9]\\d{9}", message = "Enter a valid 10-digit mobile number (+91XXXXXXXXXX)")
        String phone,

        /** Role to assign if this phone is registering for the first time. Defaults to CUSTOMER. */
        Role role
) {}
