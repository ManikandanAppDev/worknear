package com.worknear.api.auth;

import com.worknear.api.auth.dto.AuthResponse;
import com.worknear.api.auth.dto.OtpRequest;
import com.worknear.api.auth.dto.OtpRequestResponse;
import com.worknear.api.auth.dto.OtpVerifyRequest;
import com.worknear.api.auth.dto.RefreshRequest;
import com.worknear.api.common.exception.ForbiddenException;
import com.worknear.api.common.exception.UnauthorizedException;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.professional.ProfessionalProfileRepository;
import com.worknear.api.security.JwtService;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.Role;
import com.worknear.api.user.domain.User;
import com.worknear.api.user.domain.UserStatus;
import com.worknear.api.user.dto.UserResponse;
import com.worknear.api.wallet.WalletService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final WalletService walletService;

    public OtpRequestResponse requestOtp(OtpRequest request) {
        String devCode = otpService.request(request.phone());
        return new OtpRequestResponse(request.phone(), true, devCode);
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        otpService.verify(request.phone(), request.code());

        boolean[] isNew = {false};
        Role requestedRole = request.role() == null ? Role.CUSTOMER : request.role();
        if (requestedRole == Role.ADMIN) {
            // Admins are provisioned out-of-band; they cannot self-register.
            requestedRole = Role.CUSTOMER;
        }
        final Role roleForCreation = requestedRole;

        User user = userRepository.findByPhone(request.phone()).orElseGet(() -> {
            isNew[0] = true;
            return createUser(request.phone(), roleForCreation);
        });

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException("This account is blocked. Contact support.");
        }

        return issueTokens(user, isNew[0]);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtService.parse(request.refreshToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        if (!jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("Provided token is not a refresh token");
        }
        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists"));
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException("This account is blocked. Contact support.");
        }
        return issueTokens(user, false);
    }

    private User createUser(String phone, Role role) {
        User user = new User();
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        walletService.getOrCreate(user.getId());

        if (role == Role.PROFESSIONAL) {
            ProfessionalProfile profile = new ProfessionalProfile();
            profile.setUserId(user.getId());
            professionalProfileRepository.save(profile);
        }
        return user;
    }

    private AuthResponse issueTokens(User user, boolean newUser) {
        String access = jwtService.generateAccessToken(user.getId(), user.getPhone(), user.getRole());
        String refresh = jwtService.generateRefreshToken(user.getId());
        return new AuthResponse(access, refresh, "Bearer", jwtService.accessTokenTtlSeconds(),
                newUser, UserResponse.from(user));
    }
}
