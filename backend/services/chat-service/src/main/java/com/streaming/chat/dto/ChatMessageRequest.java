package com.streaming.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatMessageRequest(
        @NotNull UUID streamId,
        @NotBlank @Size(max = 500) String message
) {}