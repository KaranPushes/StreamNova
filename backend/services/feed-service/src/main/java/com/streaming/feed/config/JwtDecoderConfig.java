package com.streaming.feed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(JwtProperties properties) {

        String secret = properties.secretKey();

        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT secret key is not configured"
            );
        }

        byte[] decodedSecret =
                Base64.getDecoder().decode(secret);

        SecretKey key =
                new javax.crypto.spec.SecretKeySpec(
                        decodedSecret,
                        "HmacSHA256"
                );

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(key)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefault()
        );

        return decoder;
    }
}