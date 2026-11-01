package com.streaming.streamservice.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateStreamRequest(

        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @Size(max = 100)
        String category,

        UUID thumbnailId
) {}