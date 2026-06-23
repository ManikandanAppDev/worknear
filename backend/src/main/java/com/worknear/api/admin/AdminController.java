package com.worknear.api.admin;

import com.worknear.api.admin.dto.DashboardResponse;
import com.worknear.api.admin.dto.DisputeResponse;
import com.worknear.api.admin.dto.ResolveDisputeRequest;
import com.worknear.api.admin.dto.VerificationDecisionRequest;
import com.worknear.api.admin.dto.VerificationQueueItem;
import com.worknear.api.booking.domain.BookingStatus;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.dispute.domain.DisputeStatus;
import com.worknear.api.payout.PayoutService;
import com.worknear.api.payout.domain.PayoutStatus;
import com.worknear.api.payout.dto.PayoutResponse;
import com.worknear.api.professional.domain.VerificationStatus;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final PayoutService payoutService;

    @Operation(summary = "Dashboard KPIs")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }

    @Operation(summary = "Professional verification queue")
    @GetMapping("/verifications")
    public ApiResponse<PageResponse<VerificationQueueItem>> verifications(
            @RequestParam(required = false) VerificationStatus status, Pageable pageable) {
        return ApiResponse.ok(adminService.verificationQueue(status, pageable));
    }

    @Operation(summary = "Approve / reject / request more info for a professional")
    @PostMapping("/verifications/{profileId}/decision")
    public ApiResponse<VerificationQueueItem> decide(@PathVariable UUID profileId,
                                                    @Valid @RequestBody VerificationDecisionRequest request) {
        return ApiResponse.ok(adminService.decideVerification(profileId, request));
    }

    @Operation(summary = "List/manage bookings")
    @GetMapping("/bookings")
    public ApiResponse<PageResponse<BookingResponse>> bookings(@RequestParam(required = false) BookingStatus status,
                                                               Pageable pageable) {
        return ApiResponse.ok(adminService.bookings(status, pageable));
    }

    @Operation(summary = "List disputes")
    @GetMapping("/disputes")
    public ApiResponse<PageResponse<DisputeResponse>> disputes(@RequestParam(required = false) DisputeStatus status,
                                                               Pageable pageable) {
        return ApiResponse.ok(adminService.disputes(status, pageable));
    }

    @Operation(summary = "Resolve a dispute (optionally refund the customer)")
    @PostMapping("/disputes/{disputeId}/resolve")
    public ApiResponse<DisputeResponse> resolveDispute(@CurrentUser UserPrincipal admin, @PathVariable UUID disputeId,
                                                       @Valid @RequestBody ResolveDisputeRequest request) {
        return ApiResponse.ok(adminService.resolveDispute(disputeId, admin.id(), request));
    }

    @Operation(summary = "Payout queue")
    @GetMapping("/payouts")
    public ApiResponse<PageResponse<PayoutResponse>> payouts(
            @RequestParam(required = false, defaultValue = "REQUESTED") PayoutStatus status, Pageable pageable) {
        return ApiResponse.ok(payoutService.queue(status, pageable));
    }

    @Operation(summary = "Mark a payout as paid")
    @PostMapping("/payouts/{payoutId}/paid")
    public ApiResponse<PayoutResponse> markPaid(@PathVariable UUID payoutId) {
        return ApiResponse.ok(payoutService.markPaid(payoutId));
    }

    @Operation(summary = "Mark a payout as failed (refunds the wallet)")
    @PostMapping("/payouts/{payoutId}/failed")
    public ApiResponse<PayoutResponse> markFailed(@PathVariable UUID payoutId,
                                                 @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return ApiResponse.ok(payoutService.markFailed(payoutId, reason));
    }

    @Operation(summary = "Block or unblock a user")
    @PostMapping("/users/{userId}/block")
    public ApiResponse<Void> setBlocked(@PathVariable UUID userId, @RequestBody Map<String, Boolean> body) {
        adminService.setUserBlocked(userId, Boolean.TRUE.equals(body.get("blocked")));
        return ApiResponse.message("User updated");
    }
}
