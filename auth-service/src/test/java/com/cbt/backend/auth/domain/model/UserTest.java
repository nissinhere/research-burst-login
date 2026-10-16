package com.cbt.backend.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("given raw email, when registering, then email is trimmed and lower cased")
    void givenRawEmail_whenRegistering_thenEmailIsNormalized() {
        User user = User.register("  John.Doe@Example.COM ", "hash", NOW);

        assertThat(user.email()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("given new registration, then user is enabled without id and with equal timestamps")
    void givenNewRegistration_thenUserIsEnabledWithoutId() {
        User user = User.register("john@example.com", "hash", NOW);

        assertThat(user.id()).isNull();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.hashPassword()).isEqualTo("hash");
        assertThat(user.createdAt()).isEqualTo(NOW);
        assertThat(user.updatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("given stored values, when restoring, then all fields are preserved")
    void givenStoredValues_whenRestoring_thenAllFieldsArePreserved() {
        Instant updated = NOW.plusSeconds(60);

        User user = User.restore(7L, "john@example.com", "hash", false, NOW, updated);

        assertThat(user.id()).isEqualTo(7L);
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.createdAt()).isEqualTo(NOW);
        assertThat(user.updatedAt()).isEqualTo(updated);
    }

    @Test
    @DisplayName("given null email, when registering, then it is rejected")
    void givenNullEmail_whenRegistering_thenRejected() {
        assertThatThrownBy(() -> User.register(null, "hash", NOW)).isInstanceOf(NullPointerException.class);
    }
}
