package com.worknear.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 120) String fullName,
        @Email @Size(max = 160) String email,
        @Size(max = 512) String avatarUrl
) {}
