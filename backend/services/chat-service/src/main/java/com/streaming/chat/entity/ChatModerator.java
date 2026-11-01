package com.streaming.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chat_moderators",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_moderator_stream_user",
                        columnNames = {"stream_id", "user_id"}
                )
        }
)
public class ChatModerator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stream_id", nullable = false)
    private UUID streamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChatModerator() {
    }

    public ChatModerator(UUID streamId, UUID userId) {
        this.streamId = streamId;
        this.userId = userId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}