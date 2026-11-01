package com.streaming.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/internal/**")
                        .denyAll()

                        // Health checks
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        )
                        .permitAll()

                        // Authentication endpoints
                        .requestMatchers("/auth/**")
                        .permitAll()

                        // Public user endpoints
                        .requestMatchers(
                                "/users/profile/**",
                                "/users/search",
                                "/users/availability"
                        )
                        .permitAll()

                        // Public Channel endpoints
                        .requestMatchers(
                                "/channels/profile/**",
                                "/channels/availability",
                                "/channels/search"
                        )
                        .permitAll()

                        // Public WebSockets
                        .requestMatchers(
                                "/api/chat/**"
                        ).permitAll()

                        // Public stream reads
                        .requestMatchers(
                                HttpMethod.GET,
                                "/streams/live",
                                "/streams/channel/*",
                                "/streams/*"
                        ).permitAll()

                        // Public Follow Endpoints
                        .requestMatchers("/follows/channels/*/count")
                        .permitAll()

                        // Public Feed endpoints
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/feed/home",
                                "/api/feed/live",
                                "/api/feed/channels",
                                "/api/feed/search",
                                "/api/feed/categories",
                                "/api/feed/categories/*"
                        )
                        .permitAll()

                        // Public Media reads
                        .requestMatchers(
                                "/media/files/**",
                                "/error"
                        )
                        .permitAll()

                        // Everything else requires JWT
                        .anyRequest()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {})
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Add any frontend domain you host on here (localhost for dev, actual domain for prod)
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000", "https://*.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}