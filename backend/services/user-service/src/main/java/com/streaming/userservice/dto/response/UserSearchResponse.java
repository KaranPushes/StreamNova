package com.streaming.userservice.dto.response;

import lombok.Builder;

@Builder
public record UserSearchResponse(

        String username,
        String displayName,
        String avatarUrl

) {
}