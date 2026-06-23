package com.worknear.api.chat;

import com.worknear.api.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findByThreadIdOrderByCreatedAtDesc(UUID threadId, Pageable pageable);

    @Modifying
    @Query("update ChatMessage m set m.read = true where m.threadId = :threadId and m.senderId <> :readerId and m.read = false")
    int markReadForReader(@Param("threadId") UUID threadId, @Param("readerId") UUID readerId);
}
