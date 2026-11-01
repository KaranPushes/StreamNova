package com.streaming.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.security.account-lock")
@Getter
@Setter
public class AccountLockProperties {

    private int maxAttempts;

    private Duration duration;
}