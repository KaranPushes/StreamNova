package com.streaming.authservice.security.impl;

import com.streaming.authservice.config.JwtProperties;
import com.streaming.authservice.constants.JwtClaims;
import com.streaming.authservice.entity.Role;
import com.streaming.authservice.security.JwtService;
import com.streaming.authservice.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(
            UUID userId,
            UUID sessionId,
            String email,
            String username,
            List<Role> roles) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(JwtClaims.SESSION_ID, sessionId.toString())
                .claim(JwtClaims.EMAIL, email)
                .claim(JwtClaims.USERNAME, username)
                .claim(
                        JwtClaims.ROLES,
                        roles.stream()
                                .map(Role::name)
                                .toList()
                )
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(
                        now.plus(jwtProperties.getAccessTokenExpiration())
                ))
                .signWith(getSigningKey())
                .compact();
    }


    public UUID extractUserId(String token) {

        String subject = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();

        return UUID.fromString(subject);
    }

    @Override
    public boolean isTokenValid(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (Exception ex) {

            return false;
        }
    }

    @Override
    public JwtUserPrincipal extractPrincipal(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        UUID userId = UUID.fromString(claims.getSubject());
        UUID sessionId = UUID.fromString(
                claims.get(JwtClaims.SESSION_ID, String.class)
        );
        String email = claims.get(JwtClaims.EMAIL, String.class);
        String username = claims.get(JwtClaims.USERNAME, String.class);

        // Safely extract the raw list and map elements
        List<?> rawRoles = claims.get(JwtClaims.ROLES, List.class);
        List<Role> roles = rawRoles == null ? List.of() : rawRoles.stream()
                .map(Object::toString)
                .map(Role::valueOf)
                .toList();

        return new JwtUserPrincipal(
                userId,
                sessionId,
                email,
                username,
                roles
        );
    }

    @Override
    public String generateServiceToken(String serviceName) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(serviceName)
                .claim("type", "service")
                .claim(
                        JwtClaims.ROLES,
                        List.of(Role.SERVICE.name())
                )
                .setIssuedAt(Date.from(now))
                .setExpiration(
                        Date.from(now.plusSeconds(300))
                )
                .signWith(getSigningKey())
                .compact();
    }
}