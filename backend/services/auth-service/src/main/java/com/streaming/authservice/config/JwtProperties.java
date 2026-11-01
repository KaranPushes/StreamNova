package com.streaming.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {

    private String secretKey;

    private Duration accessTokenExpiration;

    private Duration refreshTokenExpiration;
}