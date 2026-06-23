package com.worknear.api.dispute;

import com.worknear.api.admin.dto.DisputeResponse;
import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.dispute.dto.CreateDisputeRequest;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Disputes")
@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @Operation(summary = "Raise a dispute on a booking")
    @PostMapping
    public ApiResponse<DisputeResponse> raise(@CurrentUser UserPrincipal user,
                                             @Valid @RequestBody CreateDisputeRequest request) {
        return ApiResponse.ok(DisputeResponse.from(disputeService.raise(user.id(), request)));
    }
}
