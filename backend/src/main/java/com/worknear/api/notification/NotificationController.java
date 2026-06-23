package com.worknear.api.notification;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.notification.dto.DeviceTokenRequest;
import com.worknear.api.notification.dto.NotificationResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List my notifications")
    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(@CurrentUser UserPrincipal user, Pageable pageable) {
        return ApiResponse.ok(notificationService.list(user.id(), pageable));
    }

    @Operation(summary = "Unread notification count")
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(Map.of("count", notificationService.unreadCount(user.id())));
    }

    @Operation(summary = "Mark all notifications read")
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@CurrentUser UserPrincipal user) {
        notificationService.markAllRead(user.id());
        return ApiResponse.message("All notifications marked read");
    }

    @Operation(summary = "Register a device push token")
    @PostMapping("/devices")
    public ApiResponse<Void> register(@CurrentUser UserPrincipal user, @Valid @RequestBody DeviceTokenRequest request) {
        notificationService.registerDevice(user.id(), request);
        return ApiResponse.message("Device registered");
    }

    @Operation(summary = "Unregister a device push token")
    @DeleteMapping("/devices/{token}")
    public ApiResponse<Void> unregister(@PathVariable String token) {
        notificationService.unregisterDevice(token);
        return ApiResponse.message("Device unregistered");
    }
}
