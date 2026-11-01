package com.streaming.followservice.security.impl;

import com.streaming.followservice.config.JwtProperties;
import com.streaming.followservice.security.ServiceTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceTokenProviderImpl
        implements ServiceTokenProvider {

    private final JwtProperties jwtProperties;

    @Override
    public String generateServiceToken() {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject("FOLLOW-SERVICE")
                .claim("roles", List.of("SERVICE"))
                .setIssuedAt(Date.from(now))
                .setExpiration(
                        Date.from(now.plus(5, ChronoUnit.MINUTES))
                )
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(
                jwtProperties.getSecretKey()
        );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}