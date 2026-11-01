package com.streaming.userservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateAvatarRequest(

        @NotNull(message = "Avatar media ID is required")
        UUID mediaId
) {
}