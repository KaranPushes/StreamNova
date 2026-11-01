package com.streaming.userservice.security;

public interface JwtService {

    String generateServiceToken(String serviceName);
}