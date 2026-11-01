package com.streaming.chat.security;

import com.streaming.chat.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateServiceToken(String serviceName) {

        SecretKey secretKey =
                new SecretKeySpec(
                        Base64.getDecoder()
                                .decode(jwtProperties.getSecretKey()),
                        "HmacSHA256"
                );

        return Jwts.builder()
                .subject(serviceName)
                .claim("roles", List.of("SERVICE"))
                .signWith(secretKey)
                .compact();
    }
}