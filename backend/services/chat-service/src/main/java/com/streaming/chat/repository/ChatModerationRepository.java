package com.streaming.chat.repository;

import com.streaming.chat.entity.ChatModeration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ChatModerationRepository
        extends JpaRepository<ChatModeration, UUID> {

    List<ChatModeration>
    findByStreamIdAndUserIdOrderByCreatedAtDesc(
            UUID streamId,
            UUID userId
    );

    @Modifying
    @Transactional
    void deleteByStreamIdAndUserIdAndType(
            UUID streamId,
            UUID userId,
            ChatModeration.ModerationType type
    );
}