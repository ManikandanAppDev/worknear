package com.worknear.api.chat;

import com.worknear.api.chat.domain.ChatThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatThreadRepository extends JpaRepository<ChatThread, UUID> {
    Optional<ChatThread> findByBookingId(UUID bookingId);

    @org.springframework.data.jpa.repository.Query(
            "select t from ChatThread t where t.customerId = :userId or t.professionalId = :userId order by t.lastMessageAt desc nulls last")
    List<ChatThread> findThreadsForUser(@org.springframework.data.repository.query.Param("userId") UUID userId);
}
