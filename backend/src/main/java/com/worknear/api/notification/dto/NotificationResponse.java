package com.worknear.api.notification.dto;

import com.worknear.api.notification.domain.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String body,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getBody(), n.isRead(), n.getCreatedAt());
    }
}
