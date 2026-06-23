package com.worknear.api.auth;

import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.config.WorkNearProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Issues and verifies one-time passcodes. Codes live in Redis with a TTL.
 *
 * <p>When {@code worknear.otp.mock=true} no SMS is sent and a fixed code is used,
 * so the whole flow works offline. Swap in MSG91/Twilio by setting mock=false and
 * wiring a real SMS sender in {@link #dispatch}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String CODE_KEY = "otp:code:";
    private static final String ATTEMPTS_KEY = "otp:attempts:";

    private final StringRedisTemplate redis;
    private final WorkNearProperties properties;
    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a code, stores it, and dispatches it. Returns the code only in mock
     * mode so developers can complete the flow without an SMS gateway.
     */
    public String request(String phone) {
        WorkNearProperties.Otp otp = properties.otp();
        String code = otp.mock() ? otp.mockCode() : generate(otp.length());

        redis.opsForValue().set(CODE_KEY + phone, code, Duration.ofSeconds(otp.ttlSeconds()));
        redis.delete(ATTEMPTS_KEY + phone);
        dispatch(phone, code);

        return otp.mock() ? code : null;
    }

    public void verify(String phone, String code) {
        WorkNearProperties.Otp otp = properties.otp();
        String key = CODE_KEY + phone;
        String stored = redis.opsForValue().get(key);
        if (stored == null) {
            throw new BadRequestException("OTP_EXPIRED", "OTP expired or not requested. Please request a new code.");
        }

        Long attempts = redis.opsForValue().increment(ATTEMPTS_KEY + phone);
        redis.expire(ATTEMPTS_KEY + phone, otp.ttlSeconds(), TimeUnit.SECONDS);
        if (attempts != null && attempts > otp.maxAttempts()) {
            redis.delete(key);
            throw new BadRequestException("OTP_TOO_MANY_ATTEMPTS", "Too many attempts. Please request a new code.");
        }

        if (!stored.equals(code)) {
            throw new BadRequestException("OTP_INVALID", "Invalid OTP code");
        }
        redis.delete(key);
        redis.delete(ATTEMPTS_KEY + phone);
    }

    private String generate(int length) {
        int bound = (int) Math.pow(10, length);
        return String.format("%0" + length + "d", random.nextInt(bound));
    }

    private void dispatch(String phone, String code) {
        if (properties.otp().mock()) {
            log.info("[MOCK OTP] {} -> {}", phone, code);
        } else {
            // TODO: integrate real SMS provider (MSG91/Twilio).
            log.info("Dispatching OTP to {}", phone);
        }
    }
}
