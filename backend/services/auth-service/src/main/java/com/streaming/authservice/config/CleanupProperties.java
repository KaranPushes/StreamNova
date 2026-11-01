package com.streaming.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.cleanup")
public class CleanupProperties {

    /**
     * How long revoked sessions are retained.
     */
    private Duration revokedSessionRetention = Duration.ofDays(30);

    /**
     * How long verified email tokens are retained.
     */
    private Duration verifiedEmailTokenRetention = Duration.ofDays(30);

    /**
     * How long used password reset tokens are retained.
     */
    private Duration usedPasswordResetTokenRetention = Duration.ofDays(30);
}