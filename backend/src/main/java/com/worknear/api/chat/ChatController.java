package com.worknear.api.chat;

import com.worknear.api.chat.dto.ChatMessageResponse;
import com.worknear.api.chat.dto.ChatThreadResponse;
import com.worknear.api.chat.dto.SendMessageRequest;
import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chat")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "List my chat threads")
    @GetMapping("/threads")
    public ApiResponse<List<ChatThreadResponse>> threads(@CurrentUser UserPrincipal user) {
        return ApiResponse.ok(chatService.myThreads(user.id()));
    }

    @Operation(summary = "Get the chat thread for a booking")
    @GetMapping("/threads/by-booking/{bookingId}")
    public ApiResponse<ChatThreadResponse> threadByBooking(@CurrentUser UserPrincipal user,
                                                           @PathVariable UUID bookingId) {
        return ApiResponse.ok(chatService.getThreadByBooking(user.id(), bookingId));
    }

    @Operation(summary = "List messages in a thread (most recent first)")
    @GetMapping("/threads/{threadId}/messages")
    public ApiResponse<PageResponse<ChatMessageResponse>> messages(@CurrentUser UserPrincipal user,
                                                                   @PathVariable UUID threadId,
                                                                   Pageable pageable) {
        return ApiResponse.ok(chatService.messages(user.id(), threadId, pageable));
    }

    @Operation(summary = "Send a message")
    @PostMapping("/threads/{threadId}/messages")
    public ApiResponse<ChatMessageResponse> send(@CurrentUser UserPrincipal user, @PathVariable UUID threadId,
                                                @Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.ok(chatService.send(user.id(), threadId, request));
    }

    @Operation(summary = "Mark a thread's incoming messages as read")
    @PostMapping("/threads/{threadId}/read")
    public ApiResponse<Void> markRead(@CurrentUser UserPrincipal user, @PathVariable UUID threadId) {
        chatService.markRead(user.id(), threadId);
        return ApiResponse.message("Marked read");
    }
}
