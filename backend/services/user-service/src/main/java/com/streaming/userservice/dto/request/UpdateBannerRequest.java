package com.streaming.userservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateBannerRequest(

        @NotNull(message = "Banner media ID is required")
        UUID mediaId
) {
}