package com.streaming.authservice.service.impl;

import com.streaming.authservice.config.JwtProperties;
import com.streaming.authservice.constants.SecurityConstants;
import com.streaming.authservice.dto.client.UserResponse;
import com.streaming.authservice.dto.response.LoginResponse;
import com.streaming.authservice.dto.response.RefreshTokenResponse;
import com.streaming.authservice.entity.Role;
import com.streaming.authservice.security.JwtService;
import com.streaming.authservice.security.SecureTokenGenerator;
import com.streaming.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecureTokenGenerator secureTokenGenerator;

    @Override
    public String generateAccessToken(
            UserResponse user,
            Role role,
            UUID sessionId) {

        return jwtService.generateAccessToken(
                user.id(),
                sessionId,
                user.email(),
                user.username(),
                List.of(role)
        );
    }

    @Override
    public String generateRefreshToken() {
        return secureTokenGenerator.generate();
    }

    @Override
    public LoginResponse buildLoginResponse(
            String accessToken,
            String refreshToken) {

        return new LoginResponse(
                accessToken,
                refreshToken,
                SecurityConstants.TOKEN_TYPE,
                jwtProperties.getAccessTokenExpiration().toSeconds()
        );
    }

    @Override
    public RefreshTokenResponse buildRefreshTokenResponse(
            String accessToken,
            String refreshToken) {

        return new RefreshTokenResponse(
                accessToken,
                refreshToken,
                SecurityConstants.TOKEN_TYPE,
                jwtProperties.getAccessTokenExpiration().toSeconds()
        );
    }
}