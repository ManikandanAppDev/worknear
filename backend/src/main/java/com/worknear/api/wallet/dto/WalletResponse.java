package com.worknear.api.wallet.dto;

import com.worknear.api.wallet.domain.Wallet;

import java.math.BigDecimal;

public record WalletResponse(
        BigDecimal balance,
        String currency
) {
    public static WalletResponse from(Wallet w) {
        return new WalletResponse(w.getBalance(), w.getCurrency());
    }
}
