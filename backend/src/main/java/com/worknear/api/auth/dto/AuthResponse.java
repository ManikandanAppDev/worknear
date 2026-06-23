package com.worknear.api.auth.dto;

import com.worknear.api.user.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean newUser,
        UserResponse user
) {}
