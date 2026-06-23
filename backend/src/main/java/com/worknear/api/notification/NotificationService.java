package com.worknear.api.notification;

import com.worknear.api.common.web.PageResponse;
import com.worknear.api.notification.domain.DeviceToken;
import com.worknear.api.notification.domain.Notification;
import com.worknear.api.notification.dto.DeviceTokenRequest;
import com.worknear.api.notification.dto.NotificationResponse;
import com.worknear.api.notification.push.PushSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    /**
     * Persists an in-app notification and fires a push (stubbed). Safe to call from
     * other services as a side effect of domain events.
     */
    @Transactional
    public void notifyUser(UUID userId, String type, String title, String body) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        notificationRepository.save(n);
        pushSender.send(userId, title, body);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable),
                NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllRead(userId);
    }

    @Transactional
    public void registerDevice(UUID userId, DeviceTokenRequest request) {
        DeviceToken token = deviceTokenRepository.findByToken(request.token())
                .orElseGet(DeviceToken::new);
        token.setUserId(userId);
        token.setToken(request.token());
        token.setPlatform(request.platform());
        deviceTokenRepository.save(token);
    }

    @Transactional
    public void unregisterDevice(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(deviceTokenRepository::delete);
    }
}
