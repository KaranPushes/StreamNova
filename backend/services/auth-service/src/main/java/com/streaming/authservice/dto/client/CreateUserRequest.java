package com.streaming.authservice.dto.client;

import lombok.Builder;

@Builder
public record CreateUserRequest(

        String email,

        String username,

        String displayName
) {
}