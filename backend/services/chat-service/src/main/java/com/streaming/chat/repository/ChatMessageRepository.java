package com.streaming.chat.repository;

import com.streaming.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByStreamIdOrderByCreatedAtDesc(
            UUID streamId,
            Pageable pageable
    );

    Optional<ChatMessage> findByIdAndUserId(
            UUID id,
            UUID userId
    );
}