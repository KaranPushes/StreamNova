package com.streaming.chat.repository;

import com.streaming.chat.entity.ChatModerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ChatModeratorRepository
        extends JpaRepository<ChatModerator, UUID> {

    boolean existsByStreamIdAndUserId(
            UUID streamId,
            UUID userId
    );

    Optional<ChatModerator> findByStreamIdAndUserId(
            UUID streamId,
            UUID userId
    );

    @Transactional
    void deleteByStreamIdAndUserId(
            UUID streamId,
            UUID userId
    );
}