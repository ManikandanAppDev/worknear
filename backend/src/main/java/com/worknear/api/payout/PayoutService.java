package com.worknear.api.payout;

import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.notification.NotificationService;
import com.worknear.api.payout.domain.Payout;
import com.worknear.api.payout.domain.PayoutStatus;
import com.worknear.api.payout.dto.PayoutRequestDto;
import com.worknear.api.payout.dto.PayoutResponse;
import com.worknear.api.professional.ProfessionalBankAccountRepository;
import com.worknear.api.professional.ProfessionalProfileRepository;
import com.worknear.api.professional.domain.BankMethod;
import com.worknear.api.professional.domain.ProfessionalBankAccount;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.wallet.WalletService;
import com.worknear.api.wallet.domain.TransactionReason;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final ProfessionalProfileRepository profileRepository;
    private final ProfessionalBankAccountRepository bankAccountRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    /**
     * Requests a withdrawal. Debits the pro's wallet immediately to reserve funds;
     * an admin then marks it PAID (or FAILED, which refunds).
     */
    @Transactional
    public PayoutResponse request(UUID proUserId, PayoutRequestDto req) {
        ProfessionalProfile profile = profileRepository.findByUserId(proUserId)
                .orElseThrow(() -> NotFoundException.of("Professional profile", proUserId));
        ProfessionalBankAccount bank = bankAccountRepository.findByProfessionalId(profile.getId())
                .orElseThrow(() -> new BadRequestException("Add your bank/UPI details before requesting a payout"));

        walletService.debit(proUserId, req.amount(), TransactionReason.PAYOUT, null, "Payout request");

        Payout payout = new Payout();
        payout.setProfessionalId(proUserId);
        payout.setAmount(req.amount());
        payout.setMethod(bank.getMethod());
        payout.setDestination(destination(bank));
        payout.setStatus(PayoutStatus.REQUESTED);
        return PayoutResponse.from(payoutRepository.save(payout));
    }

    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> myPayouts(UUID proUserId, Pageable pageable) {
        return PageResponse.from(
                payoutRepository.findByProfessionalIdOrderByCreatedAtDesc(proUserId, pageable),
                PayoutResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> queue(PayoutStatus status, Pageable pageable) {
        return PageResponse.from(
                payoutRepository.findByStatusOrderByCreatedAtAsc(status, pageable),
                PayoutResponse::from);
    }

    /** Admin: mark a payout as paid. */
    @Transactional
    public PayoutResponse markPaid(UUID payoutId) {
        Payout payout = require(payoutId);
        if (payout.getStatus() == PayoutStatus.PAID) {
            return PayoutResponse.from(payout);
        }
        payout.setStatus(PayoutStatus.PAID);
        payout.setProcessedAt(Instant.now());
        notificationService.notifyUser(payout.getProfessionalId(), "PAYOUT_PAID",
                "Payout processed", "Your payout of " + payout.getAmount() + " has been paid");
        return PayoutResponse.from(payout);
    }

    /** Admin: mark a payout as failed and refund the reserved amount. */
    @Transactional
    public PayoutResponse markFailed(UUID payoutId, String reason) {
        Payout payout = require(payoutId);
        if (payout.getStatus() == PayoutStatus.FAILED) {
            return PayoutResponse.from(payout);
        }
        payout.setStatus(PayoutStatus.FAILED);
        payout.setFailureReason(reason);
        payout.setProcessedAt(Instant.now());
        walletService.credit(payout.getProfessionalId(), payout.getAmount(), TransactionReason.REFUND,
                payout.getId(), "Payout failed - refund");
        notificationService.notifyUser(payout.getProfessionalId(), "PAYOUT_FAILED",
                "Payout failed", "Your payout was not processed and has been refunded to your wallet");
        return PayoutResponse.from(payout);
    }

    private Payout require(UUID payoutId) {
        return payoutRepository.findById(payoutId)
                .orElseThrow(() -> NotFoundException.of("Payout", payoutId));
    }

    private String destination(ProfessionalBankAccount bank) {
        if (bank.getMethod() == BankMethod.UPI) {
            return bank.getUpiId();
        }
        String acct = bank.getAccountNumber();
        return acct == null ? null : "A/C ****" + acct.substring(Math.max(0, acct.length() - 4));
    }
}
