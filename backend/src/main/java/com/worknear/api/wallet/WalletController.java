package com.worknear.api.wallet;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import com.worknear.api.wallet.dto.WalletResponse;
import com.worknear.api.wallet.dto.WalletTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wallet")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Get my wallet balance")
    @GetMapping
    public ApiResponse<WalletResponse> wallet(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(walletService.getWallet(user.id()));
    }

    @Operation(summary = "List my wallet transactions")
    @GetMapping("/transactions")
    public ApiResponse<PageResponse<WalletTransactionResponse>> transactions(@CurrentUser UserPrincipal user,
                                                                             Pageable pageable) {
        return ApiResponse.ok(walletService.transactions(user.id(), pageable));
    }
}
