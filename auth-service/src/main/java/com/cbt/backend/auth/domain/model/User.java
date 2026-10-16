package com.cbt.backend.auth.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public final class User {

    private final Long id;
    private final String email;
    private final String hashPassword;
    private final boolean enabled;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(Long id, String email, String hashPassword, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "email");
        this.hashPassword = Objects.requireNonNull(hashPassword, "hashPassword");
        this.enabled = enabled;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static User register(String email, String hashPassword, Instant now) {
        return new User(null, normalizeEmail(email), hashPassword, true, now, now);
    }

    public static User restore(Long id, String email, String hashPassword, boolean enabled, Instant createdAt, Instant updatedAt) {
        return new User(id, email, hashPassword, enabled, createdAt, updatedAt);
    }

    public static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "email");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public Long id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String hashPassword() {
        return hashPassword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
