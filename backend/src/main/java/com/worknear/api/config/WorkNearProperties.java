package com.worknear.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Strongly-typed binding of all {@code worknear.*} configuration.
 */
@ConfigurationProperties(prefix = "worknear")
public record WorkNearProperties(
        Jwt jwt,
        Otp otp,
        Payment payment,
        Storage storage,
        Push push,
        Cors cors
) {
    public record Jwt(
            String secret,
            long accessTokenTtlMinutes,
            long refreshTokenTtlDays,
            String issuer
    ) {}

    public record Otp(
            int length,
            long ttlSeconds,
            boolean mock,
            String mockCode,
            int maxAttempts
    ) {}

    public record Payment(
            boolean mock,
            int platformCommissionPercent
    ) {}

    public record Storage(
            boolean mock,
            String baseUrl,
            String localDir
    ) {}

    public record Push(
            boolean mock
    ) {}

    public record Cors(
            List<String> allowedOrigins
    ) {}
}
