package com.streaming.authservice.security;

import com.streaming.authservice.entity.Role;

import java.util.List;
import java.util.UUID;

public interface JwtService {

    String generateAccessToken(
            UUID userId,
            UUID sessionId,
            String email,
            String username,
            List<Role> roles);

    JwtUserPrincipal extractPrincipal(String token);

    boolean isTokenValid(String token);

    String generateServiceToken(String serviceName);
}