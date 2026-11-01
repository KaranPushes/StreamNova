package com.streaming.streamservice.dto.request;

public record StreamAuthenticationRequest(
        String user,
        String password,
        String token,
        String ip,
        String action,
        String path,
        String protocol,
        String id,
        String query,
        String userAgent
) {}