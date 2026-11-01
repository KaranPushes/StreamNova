package com.streaming.feed.security;

public interface JwtService {

    String generateServiceToken(String serviceName);
}