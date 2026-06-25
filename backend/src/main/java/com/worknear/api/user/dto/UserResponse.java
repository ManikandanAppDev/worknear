package com.worknear.api.user.dto;

import com.worknear.api.user.domain.Role;
import com.worknear.api.user.domain.User;
import com.worknear.api.user.domain.UserStatus;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String phone,
        Role role,
        String fullName,
        String email,
        String avatarUrl,
        UserStatus status,
        boolean roleConfirmed
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getPhone(), u.getRole(), u.getFullName(),
                u.getEmail(), u.getAvatarUrl(), u.getStatus(), u.isRoleConfirmed());
    }
}
