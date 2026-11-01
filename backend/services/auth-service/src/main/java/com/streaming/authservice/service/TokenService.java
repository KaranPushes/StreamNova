package com.streaming.authservice.service;

import com.streaming.authservice.dto.client.UserResponse;
import com.streaming.authservice.dto.response.LoginResponse;
import com.streaming.authservice.dto.response.RefreshTokenResponse;
import com.streaming.authservice.entity.Role;

import java.util.UUID;

public interface TokenService {

    String generateAccessToken(
            UserResponse user,
            Role role,
            UUID sessionId
    );

    String generateRefreshToken();

    LoginResponse buildLoginResponse(
            String accessToken,
            String refreshToken
    );

    RefreshTokenResponse buildRefreshTokenResponse(
            String accessToken,
            String refreshToken
    );
}