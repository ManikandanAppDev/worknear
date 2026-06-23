package com.worknear.api.notification;

import com.worknear.api.notification.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserId(UUID userId);
}
