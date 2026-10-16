package com.cbt.backend.auth.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordHasherTest {

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher(4);

    @Test
    @DisplayName("given raw password, when hashing, then hash differs from raw and matches it")
    void givenRawPassword_whenHashing_thenHashMatches() {
        String hash = hasher.hash("S3cret-pass");

        assertThat(hash).isNotEqualTo("S3cret-pass");
        assertThat(hasher.matches("S3cret-pass", hash)).isTrue();
    }

    @Test
    @DisplayName("given wrong password, when matching, then it does not match")
    void givenWrongPassword_whenMatching_thenFalse() {
        String hash = hasher.hash("S3cret-pass");

        assertThat(hasher.matches("other-pass", hash)).isFalse();
    }

    @Test
    @DisplayName("given same password hashed twice, then hashes differ because of salt")
    void givenSamePasswordHashedTwice_thenHashesDiffer() {
        assertThat(hasher.hash("S3cret-pass")).isNotEqualTo(hasher.hash("S3cret-pass"));
    }

    @Test
    @DisplayName("given malformed hash, when matching, then it does not match")
    void givenMalformedHash_whenMatching_thenFalse() {
        assertThat(hasher.matches("S3cret-pass", "not-a-bcrypt-hash")).isFalse();
    }
}
