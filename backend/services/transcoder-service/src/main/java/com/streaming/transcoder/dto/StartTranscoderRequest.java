package com.streaming.transcoder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartTranscoderRequest(
        @NotNull UUID streamId,
        @NotBlank String streamKey
) {
}