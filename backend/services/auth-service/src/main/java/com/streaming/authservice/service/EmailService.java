package com.streaming.authservice.service;

import java.util.UUID;

public interface EmailService {

    void sendEmailVerification(
            UUID userId,
            String email,
            String token
    );

    void sendPasswordResetEmail(
            UUID userId,
            String email,
            String token
    );
}