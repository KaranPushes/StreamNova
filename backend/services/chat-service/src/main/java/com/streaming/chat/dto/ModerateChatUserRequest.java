package com.streaming.chat.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ModerateChatUserRequest(
        @NotNull UUID userId,
        @NotNull ModerationAction action,
        Instant expiresAt
) {

    public enum ModerationAction {
        MUTE,
        BAN
    }
}