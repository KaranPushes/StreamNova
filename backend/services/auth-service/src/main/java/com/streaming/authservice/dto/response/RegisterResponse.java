package com.streaming.authservice.dto.response;

import java.util.UUID;

public record RegisterResponse(

        UUID userId,

        String message
) {
}