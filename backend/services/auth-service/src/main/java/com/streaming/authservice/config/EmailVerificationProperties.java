package com.streaming.authservice.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "application.security.email-verification")
public class EmailVerificationProperties {

    private Duration expiration;
}