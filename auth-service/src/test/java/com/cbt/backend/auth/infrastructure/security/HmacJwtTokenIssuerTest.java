package com.cbt.backend.auth.infrastructure.security;

import com.cbt.backend.auth.application.port.out.IssuedToken;
import com.cbt.backend.auth.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacJwtTokenIssuerTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-0123";
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final long TTL = 600;

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final HmacJwtTokenIssuer issuer = new HmacJwtTokenIssuer(SECRET, TTL, clock);
    private final User user = User.restore(42L, "john@example.com", "hash", true, NOW, NOW);

    private String decode(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("given user, when issuing, then token has three parts and ttl is reported")
    void givenUser_whenIssuing_thenTokenHasThreePartsAndTtl() {
        IssuedToken token = issuer.issue(user);

        assertThat(token.value().split("\\.")).hasSize(3);
        assertThat(token.expiresInSeconds()).isEqualTo(TTL);
    }

    @Test
    @DisplayName("given user, when issuing, then header and payload carry expected claims")
    void givenUser_whenIssuing_thenClaimsAreExpected() {
        String[] parts = issuer.issue(user).value().split("\\.");

        assertThat(decode(parts[0])).isEqualTo("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        assertThat(decode(parts[1])).isEqualTo(
                "{\"sub\":\"42\",\"email\":\"john@example.com\",\"iat\":" + NOW.getEpochSecond()
                        + ",\"exp\":" + (NOW.getEpochSecond() + TTL) + "}");
    }

    @Test
    @DisplayName("given issued token, then signature verifies with the shared secret")
    void givenIssuedToken_thenSignatureVerifies() throws Exception {
        String[] parts = issuer.issue(user).value().split("\\.");

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String expected = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8)));

        assertThat(parts[2]).isEqualTo(expected);
    }

    @Test
    @DisplayName("given short secret, when creating issuer, then it is rejected")
    void givenShortSecret_whenCreatingIssuer_thenRejected() {
        assertThatThrownBy(() -> new HmacJwtTokenIssuer("short", TTL, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("given non positive ttl, when creating issuer, then it is rejected")
    void givenNonPositiveTtl_whenCreatingIssuer_thenRejected() {
        assertThatThrownBy(() -> new HmacJwtTokenIssuer(SECRET, 0, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
