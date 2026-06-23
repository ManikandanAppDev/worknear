package com.worknear.api.wallet;

import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.wallet.domain.TransactionReason;
import com.worknear.api.wallet.domain.TransactionType;
import com.worknear.api.wallet.domain.Wallet;
import com.worknear.api.wallet.domain.WalletTransaction;
import com.worknear.api.wallet.dto.WalletResponse;
import com.worknear.api.wallet.dto.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public Wallet getOrCreate(UUID userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet w = new Wallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            return walletRepository.save(w);
        });
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        Wallet w = walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet nw = new Wallet();
            nw.setUserId(userId);
            return nw;
        });
        return WalletResponse.from(w);
    }

    @Transactional(readOnly = true)
    public PageResponse<WalletTransactionResponse> transactions(UUID userId, Pageable pageable) {
        Wallet w = getOrCreate(userId);
        return PageResponse.from(
                transactionRepository.findByWalletIdOrderByCreatedAtDesc(w.getId(), pageable),
                WalletTransactionResponse::from);
    }

    /**
     * Adds money to a wallet and records a transaction. Uses a row lock to keep the
     * balance consistent under concurrent debits/credits.
     */
    @Transactional
    public WalletTransaction credit(UUID userId, BigDecimal amount, TransactionReason reason,
                                    UUID referenceId, String description) {
        requirePositive(amount);
        getOrCreate(userId);
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId).orElseThrow();
        wallet.setBalance(wallet.getBalance().add(amount));
        return record(wallet, TransactionType.CREDIT, reason, amount, referenceId, description);
    }

    /**
     * Removes money from a wallet, failing if the balance is insufficient.
     */
    @Transactional
    public WalletTransaction debit(UUID userId, BigDecimal amount, TransactionReason reason,
                                   UUID referenceId, String description) {
        requirePositive(amount);
        getOrCreate(userId);
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId).orElseThrow();
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("INSUFFICIENT_BALANCE", "Insufficient wallet balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        return record(wallet, TransactionType.DEBIT, reason, amount, referenceId, description);
    }

    private WalletTransaction record(Wallet wallet, TransactionType type, TransactionReason reason,
                                     BigDecimal amount, UUID referenceId, String description) {
        WalletTransaction txn = new WalletTransaction();
        txn.setWalletId(wallet.getId());
        txn.setType(type);
        txn.setReason(reason);
        txn.setAmount(amount);
        txn.setBalanceAfter(wallet.getBalance());
        txn.setReferenceId(referenceId);
        txn.setDescription(description);
        return transactionRepository.save(txn);
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
    }
}
