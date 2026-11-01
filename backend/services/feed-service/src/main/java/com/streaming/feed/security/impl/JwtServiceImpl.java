package com.streaming.feed.security.impl;

import com.streaming.feed.config.JwtProperties;
import com.streaming.feed.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    @Override
    public String generateServiceToken(String serviceName) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(serviceName)
                .claim("roles", List.of("SERVICE"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(
                        now.plusSeconds(60)
                ))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(jwtProperties.secretKey());

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }
}