package com.streaming.chat.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chat_moderations",
        indexes = {
                @Index(
                        name = "idx_chat_moderation_stream_user",
                        columnList = "stream_id, user_id"
                )
        }
)
public class ChatModeration {

    @Id
    private UUID id;

    @Column(name = "stream_id", nullable = false)
    private UUID streamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationType type;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChatModeration() {
    }

    public ChatModeration(
            UUID streamId,
            UUID userId,
            ModerationType type,
            Instant expiresAt
    ) {
        this.id = UUID.randomUUID();
        this.streamId = streamId;
        this.userId = userId;
        this.type = type;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ModerationType getType() {
        return type;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return expiresAt == null ||
                expiresAt.isAfter(Instant.now());
    }

    public enum ModerationType {
        MUTE,
        BAN
    }
}