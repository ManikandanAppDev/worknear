package com.worknear.api.notification.push;

import com.worknear.api.notification.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Logs the push instead of contacting FCM. Active while {@code worknear.push.mock=true}
 * (the only implementation for now).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockPushSender implements PushSender {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public void send(UUID userId, String title, String body) {
        var tokens = deviceTokenRepository.findByUserId(userId);
        log.info("[MOCK PUSH] -> user={} devices={} title='{}' body='{}'", userId, tokens.size(), title, body);
    }
}
