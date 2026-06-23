package com.worknear.api.auth;

import com.worknear.api.auth.dto.AuthResponse;
import com.worknear.api.auth.dto.OtpRequest;
import com.worknear.api.auth.dto.OtpRequestResponse;
import com.worknear.api.auth.dto.OtpVerifyRequest;
import com.worknear.api.auth.dto.RefreshRequest;
import com.worknear.api.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirements // public endpoints — no bearer token required
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Request an OTP for a phone number")
    @PostMapping("/otp/request")
    public ApiResponse<OtpRequestResponse> requestOtp(@Valid @RequestBody OtpRequest request) {
        return ApiResponse.ok(authService.requestOtp(request));
    }

    @Operation(summary = "Verify OTP and obtain tokens (auto-registers on first login)")
    @PostMapping("/otp/verify")
    public ApiResponse<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ApiResponse.ok(authService.verifyOtp(request));
    }

    @Operation(summary = "Exchange a refresh token for a new token pair")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }
}
