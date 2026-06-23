package com.worknear.api.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeviceTokenRequest(
        @NotBlank String token,
        @NotBlank @Pattern(regexp = "ANDROID|IOS|WEB", message = "platform must be ANDROID, IOS or WEB") String platform
) {}
