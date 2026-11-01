package com.streaming.userservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserRequest(

        @Size(min = 3, max = 30,
                message = "Username must be between 3 and 30 characters")
        String username,

        @Size(min = 3, max = 50,
                message = "Display name must be between 3 and 50 characters")
        String displayName,

        @Size(max = 300,
                message = "Bio cannot exceed 300 characters")
        String bio,

        @Size(max = 500,
                message = "Website URL cannot exceed 500 characters")
        String websiteUrl,

        @Size(max = 100,
                message = "Location cannot exceed 100 characters")
        String location
) {
}