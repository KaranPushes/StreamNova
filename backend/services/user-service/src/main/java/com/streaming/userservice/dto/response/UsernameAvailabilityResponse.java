package com.streaming.userservice.dto.response;

import lombok.Builder;

@Builder
public record UsernameAvailabilityResponse(

        String username,
        boolean available

) {
}