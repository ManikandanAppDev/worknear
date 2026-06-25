package com.worknear.api.user;

import com.worknear.api.auth.AuthService;
import com.worknear.api.auth.dto.AuthResponse;
import com.worknear.api.auth.dto.OtpRequest;
import com.worknear.api.auth.dto.OtpRequestResponse;
import com.worknear.api.auth.dto.OtpVerifyRequest;
import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import com.worknear.api.user.domain.User;
import com.worknear.api.user.dto.AddressRequest;
import com.worknear.api.user.dto.AddressResponse;
import com.worknear.api.user.dto.UpdateProfileRequest;
import com.worknear.api.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User & Profile")
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Get my account")
    @GetMapping
    public ApiResponse<UserResponse> me(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(userService.getMe(user.id()));
    }

    @Operation(summary = "Request an OTP to change my phone number")
    @PostMapping("/phone/otp/request")
    public ApiResponse<OtpRequestResponse> requestPhoneChangeOtp(@CurrentUser UserPrincipal user,
                                                                 @Valid @RequestBody OtpRequest request) {
        return ApiResponse.ok(userService.requestPhoneChangeOtp(user.id(), request.phone()));
    }

    @Operation(summary = "Verify OTP and change my phone number (re-issues tokens)")
    @PostMapping("/phone/verify")
    public ApiResponse<AuthResponse> verifyPhoneChange(@CurrentUser UserPrincipal user,
                                                       @Valid @RequestBody OtpVerifyRequest request) {
        User updated = userService.changePhone(user.id(), request.phone(), request.code());
        return ApiResponse.ok(authService.issueTokensForExistingUser(updated));
    }

    @Operation(summary = "Update my profile")
    @PatchMapping
    public ApiResponse<UserResponse> updateProfile(@CurrentUser UserPrincipal user,
                                                   @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(user.id(), request));
    }

    @Operation(summary = "List my saved addresses")
    @GetMapping("/addresses")
    public ApiResponse<List<AddressResponse>> addresses(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(userService.listAddresses(user.id()));
    }

    @Operation(summary = "Add an address")
    @PostMapping("/addresses")
    public ApiResponse<AddressResponse> addAddress(@CurrentUser UserPrincipal user,
                                                   @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(userService.addAddress(user.id(), request));
    }

    @Operation(summary = "Update an address")
    @PutMapping("/addresses/{addressId}")
    public ApiResponse<AddressResponse> updateAddress(@CurrentUser UserPrincipal user,
                                                      @PathVariable UUID addressId,
                                                      @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(userService.updateAddress(user.id(), addressId, request));
    }

    @Operation(summary = "Delete an address")
    @DeleteMapping("/addresses/{addressId}")
    public ApiResponse<Void> deleteAddress(@CurrentUser UserPrincipal user, @PathVariable UUID addressId) {
        userService.deleteAddress(user.id(), addressId);
        return ApiResponse.message("Address deleted");
    }
}
