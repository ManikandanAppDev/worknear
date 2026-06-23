package com.worknear.api.auth.dto;

public record OtpRequestResponse(
        String phone,
        boolean otpSent,
        /** Populated only in mock mode so devs can verify without an SMS gateway. */
        String devCode
) {}
