package com.streaming.channelservice.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getUserId(Jwt jwt) {

        if (jwt == null || jwt.getSubject() == null) {
            throw new IllegalStateException(
                    "Authenticated user ID is missing from JWT"
            );
        }

        try {
            return UUID.fromString(jwt.getSubject());

        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT subject is not a valid user ID",
                    exception
            );
        }
    }
}