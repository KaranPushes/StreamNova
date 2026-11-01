package com.streaming.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(
            name = "refresh_token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String refreshTokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant lastAccessedAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(length = 100)
    private String deviceName;

    @Column(length = 50)
    private String browser;

    @Column(length = 50)
    private String operatingSystem;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 45)
    private String ipAddress;
}