package com.cbt.backend.auth.infrastructure.security;

import com.cbt.backend.auth.application.port.out.IssuedToken;
import com.cbt.backend.auth.application.port.out.TokenIssuer;
import com.cbt.backend.auth.domain.model.User;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.util.Base64;

public class HmacJwtTokenIssuer implements TokenIssuer {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final int MIN_SECRET_BYTES = 32;

    private final byte[] secret;
    private final long ttlSeconds;
    private final Clock clock;

    public HmacJwtTokenIssuer(String secret, long ttlSeconds, Clock clock) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("JWT secret must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("JWT ttl must be positive");
        }
        this.secret = secretBytes;
        this.ttlSeconds = ttlSeconds;
        this.clock = clock;
    }

    @Override
    public IssuedToken issue(User user) {
        long issuedAt = clock.instant().getEpochSecond();
        long expiresAt = issuedAt + ttlSeconds;
        String payload = "{\"sub\":\"" + user.id() + "\",\"email\":\"" + escape(user.email())
                + "\",\"iat\":" + issuedAt + ",\"exp\":" + expiresAt + "}";
        String signingInput = encode(HEADER) + "." + encode(payload);
        return new IssuedToken(signingInput + "." + sign(signingInput), ttlSeconds);
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
