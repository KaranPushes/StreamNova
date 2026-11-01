package com.streaming.authservice.dto.response;

    public record RefreshTokenResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresIn
) {
}