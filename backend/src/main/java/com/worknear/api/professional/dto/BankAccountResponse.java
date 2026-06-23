package com.worknear.api.professional.dto;

import com.worknear.api.professional.domain.BankMethod;
import com.worknear.api.professional.domain.ProfessionalBankAccount;

public record BankAccountResponse(
        BankMethod method,
        String upiId,
        String accountName,
        String maskedAccountNumber,
        String ifsc
) {
    public static BankAccountResponse from(ProfessionalBankAccount b) {
        if (b == null) {
            return null;
        }
        return new BankAccountResponse(b.getMethod(), b.getUpiId(), b.getAccountName(),
                mask(b.getAccountNumber()), b.getIfsc());
    }

    private static String mask(String acct) {
        if (acct == null || acct.length() < 4) {
            return acct;
        }
        return "****" + acct.substring(acct.length() - 4);
    }
}
