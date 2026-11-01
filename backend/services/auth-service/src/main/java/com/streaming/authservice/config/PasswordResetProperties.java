package com.streaming.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.security.password-reset")
@Getter
@Setter
public class PasswordResetProperties {

    private Duration expiration;
}