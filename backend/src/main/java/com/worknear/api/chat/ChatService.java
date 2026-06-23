package com.worknear.api.chat;

import com.worknear.api.chat.domain.ChatMessage;
import com.worknear.api.chat.domain.ChatThread;
import com.worknear.api.chat.dto.ChatMessageResponse;
import com.worknear.api.chat.dto.ChatThreadResponse;
import com.worknear.api.chat.dto.SendMessageRequest;
import com.worknear.api.common.exception.ForbiddenException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.notification.NotificationService;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatThreadRepository threadRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ChatThreadResponse> myThreads(UUID userId) {
        return threadRepository.findThreadsForUser(userId).stream()
                .map(t -> ChatThreadResponse.from(t, counterpartName(t, userId)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatThreadResponse getThreadByBooking(UUID userId, UUID bookingId) {
        ChatThread thread = threadRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("Chat is available only after the booking is confirmed"));
        assertParticipant(userId, thread);
        return ChatThreadResponse.from(thread, counterpartName(thread, userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> messages(UUID userId, UUID threadId, Pageable pageable) {
        ChatThread thread = requireThread(threadId);
        assertParticipant(userId, thread);
        return PageResponse.from(
                messageRepository.findByThreadIdOrderByCreatedAtDesc(threadId, pageable),
                ChatMessageResponse::from);
    }

    @Transactional
    public ChatMessageResponse send(UUID userId, UUID threadId, SendMessageRequest request) {
        ChatThread thread = requireThread(threadId);
        assertParticipant(userId, thread);

        ChatMessage message = new ChatMessage();
        message.setThreadId(threadId);
        message.setSenderId(userId);
        message.setContent(request.content());
        message = messageRepository.save(message);

        thread.setLastMessageAt(Instant.now());

        UUID recipient = userId.equals(thread.getCustomerId()) ? thread.getProfessionalId() : thread.getCustomerId();
        notificationService.notifyUser(recipient, "CHAT_MESSAGE", "New message", request.content());

        return ChatMessageResponse.from(message);
    }

    @Transactional
    public void markRead(UUID userId, UUID threadId) {
        ChatThread thread = requireThread(threadId);
        assertParticipant(userId, thread);
        messageRepository.markReadForReader(threadId, userId);
    }

    private ChatThread requireThread(UUID threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> NotFoundException.of("Chat thread", threadId));
    }

    private void assertParticipant(UUID userId, ChatThread thread) {
        if (!userId.equals(thread.getCustomerId()) && !userId.equals(thread.getProfessionalId())) {
            throw new ForbiddenException("You are not a participant in this chat");
        }
    }

    private String counterpartName(ChatThread thread, UUID userId) {
        UUID other = userId.equals(thread.getCustomerId()) ? thread.getProfessionalId() : thread.getCustomerId();
        return userRepository.findById(other).map(User::getFullName).orElse(null);
    }
}
