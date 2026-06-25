package com.worknear.api.user.dto;

import com.worknear.api.user.domain.Role;
import jakarta.validation.constraints.NotNull;

/** Sets the signed-in user's role during onboarding (CUSTOMER or PROFESSIONAL). */
public record ChangeRoleRequest(
        @NotNull Role role
) {}
