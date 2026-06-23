package com.worknear.api.professional;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.professional.domain.DocumentType;
import com.worknear.api.professional.dto.BankAccountRequest;
import com.worknear.api.professional.dto.BankAccountResponse;
import com.worknear.api.professional.dto.DocumentResponse;
import com.worknear.api.professional.dto.ProProfileResponse;
import com.worknear.api.professional.dto.SetAvailabilityRequest;
import com.worknear.api.professional.dto.SetServicesRequest;
import com.worknear.api.professional.dto.SetSpecializationsRequest;
import com.worknear.api.professional.dto.UpdateProfileRequest;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Professional (Self)")
@RestController
@RequestMapping("/api/v1/pro")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalController {

    private final ProfessionalProfileService profileService;

    @Operation(summary = "Get my professional profile (onboarding state)")
    @GetMapping("/profile")
    public ApiResponse<ProProfileResponse> myProfile(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(profileService.getMyProfile(user.id()));
    }

    @Operation(summary = "Update my professional profile")
    @PatchMapping("/profile")
    public ApiResponse<ProProfileResponse> updateProfile(@CurrentUser UserPrincipal user,
                                                         @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(profileService.updateProfile(user.id(), request));
    }

    @Operation(summary = "Set the services I offer (and their prices)")
    @PutMapping("/services")
    public ApiResponse<ProProfileResponse> setServices(@CurrentUser UserPrincipal user,
                                                       @Valid @RequestBody SetServicesRequest request) {
        return ApiResponse.ok(profileService.setServices(user.id(), request));
    }

    @Operation(summary = "Set my specializations")
    @PutMapping("/specializations")
    public ApiResponse<ProProfileResponse> setSpecializations(@CurrentUser UserPrincipal user,
                                                              @Valid @RequestBody SetSpecializationsRequest request) {
        return ApiResponse.ok(profileService.setSpecializations(user.id(), request));
    }

    @Operation(summary = "Set my weekly availability")
    @PutMapping("/availability")
    public ApiResponse<ProProfileResponse> setAvailability(@CurrentUser UserPrincipal user,
                                                          @Valid @RequestBody SetAvailabilityRequest request) {
        return ApiResponse.ok(profileService.setAvailability(user.id(), request));
    }

    @Operation(summary = "Upload a verification document")
    @PostMapping(value = "/documents", consumes = "multipart/form-data")
    public ApiResponse<DocumentResponse> uploadDocument(@CurrentUser UserPrincipal user,
                                                        @RequestParam DocumentType type,
                                                        @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(profileService.uploadDocument(user.id(), type, file));
    }

    @Operation(summary = "Set my payout (bank/UPI) details")
    @PutMapping("/bank-account")
    public ApiResponse<BankAccountResponse> setBankAccount(@CurrentUser UserPrincipal user,
                                                          @Valid @RequestBody BankAccountRequest request) {
        return ApiResponse.ok(profileService.setBankAccount(user.id(), request));
    }

    @Operation(summary = "Toggle my online/offline status")
    @PatchMapping("/online")
    public ApiResponse<ProProfileResponse> setOnline(@CurrentUser UserPrincipal user,
                                                    @RequestBody Map<String, Boolean> body) {
        boolean online = Boolean.TRUE.equals(body.get("online"));
        return ApiResponse.ok(profileService.setOnline(user.id(), online));
    }

    @Operation(summary = "Submit my profile for admin verification")
    @PostMapping("/submit-verification")
    public ApiResponse<ProProfileResponse> submit(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(profileService.submitForVerification(user.id()));
    }
}
