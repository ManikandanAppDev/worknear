package com.worknear.api.chat.dto;

import com.worknear.api.chat.domain.ChatMessage;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID threadId,
        UUID senderId,
        String content,
        boolean read,
        Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getThreadId(), m.getSenderId(),
                m.getContent(), m.isRead(), m.getCreatedAt());
    }
}
