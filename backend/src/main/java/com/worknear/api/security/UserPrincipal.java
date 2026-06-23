package com.worknear.api.security;

import com.worknear.api.user.domain.Role;

import java.util.UUID;

/**
 * Immutable authenticated principal stored in the SecurityContext.
 */
public record UserPrincipal(UUID id, String phone, Role role) {
}
