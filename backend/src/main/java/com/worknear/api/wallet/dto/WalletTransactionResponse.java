package com.worknear.api.wallet.dto;

import com.worknear.api.wallet.domain.TransactionReason;
import com.worknear.api.wallet.domain.TransactionType;
import com.worknear.api.wallet.domain.WalletTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        TransactionType type,
        TransactionReason reason,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {
    public static WalletTransactionResponse from(WalletTransaction t) {
        return new WalletTransactionResponse(
                t.getId(), t.getType(), t.getReason(), t.getAmount(),
                t.getBalanceAfter(), t.getDescription(), t.getCreatedAt());
    }
}
