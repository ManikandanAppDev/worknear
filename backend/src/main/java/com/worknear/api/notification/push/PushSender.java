package com.worknear.api.notification.push;

import java.util.UUID;

/**
 * Abstraction over a push provider (FCM). The mock implementation just logs.
 */
public interface PushSender {
    void send(UUID userId, String title, String body);
}
