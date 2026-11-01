package com.streaming.transcoder.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StopTranscoderRequest(
        @NotNull UUID streamId
) {
}