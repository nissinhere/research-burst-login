package com.cbt.backend.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(Jwt jwt, Password password) {

    public record Jwt(String secret, long ttlSeconds) {
    }

    public record Password(int bcryptStrength) {
    }
}
