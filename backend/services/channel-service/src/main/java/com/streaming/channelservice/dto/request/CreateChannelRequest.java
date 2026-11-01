package com.streaming.channelservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(

        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "^[a-zA-Z0-9_-]+$",
                message = "Slug can only contain letters, numbers, underscores and hyphens"
        )
        String slug,

        @NotBlank
        @Size(min = 2, max = 100)
        String displayName

) {
}