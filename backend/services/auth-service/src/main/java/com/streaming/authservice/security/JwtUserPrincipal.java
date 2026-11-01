package com.streaming.authservice.security;

import com.streaming.authservice.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public record JwtUserPrincipal(

        UUID userId,

        UUID sessionId,

        String email,

        String username,

        List<Role> roles
) {

    public List<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .<GrantedAuthority>map(role ->
                        new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }
}