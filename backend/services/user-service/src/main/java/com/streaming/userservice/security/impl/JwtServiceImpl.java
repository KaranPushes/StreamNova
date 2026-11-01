package com.streaming.userservice.security.impl;

import com.streaming.userservice.config.JwtProperties;
import com.streaming.userservice.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    @Override
    public String generateServiceToken(String serviceName) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(serviceName)
                .claim("roles", java.util.List.of("SERVICE"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(
                        now.plusSeconds(60)
                ))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(
                        jwtProperties.getSecretKey()
                );

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }
}