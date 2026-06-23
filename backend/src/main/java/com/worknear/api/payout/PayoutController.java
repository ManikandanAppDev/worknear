package com.worknear.api.payout;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.payout.dto.PayoutRequestDto;
import com.worknear.api.payout.dto.PayoutResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payouts (Professional)")
@RestController
@RequestMapping("/api/v1/pro/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROFESSIONAL')")
public class PayoutController {

    private final PayoutService payoutService;

    @Operation(summary = "Request a payout/withdrawal")
    @PostMapping
    public ApiResponse<PayoutResponse> request(@CurrentUser UserPrincipal user,
                                              @Valid @RequestBody PayoutRequestDto request) {
        return ApiResponse.ok(payoutService.request(user.id(), request));
    }

    @Operation(summary = "List my payouts")
    @GetMapping
    public ApiResponse<PageResponse<PayoutResponse>> myPayouts(@CurrentUser UserPrincipal user, Pageable pageable) {
        return ApiResponse.ok(payoutService.myPayouts(user.id(), pageable));
    }
}
