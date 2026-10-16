package com.cbt.backend.auth.infrastructure.security;

import com.cbt.backend.auth.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public BcryptPasswordHasher(int strength) {
        this.encoder = new BCryptPasswordEncoder(strength);
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashPassword) {
        try {
            return encoder.matches(rawPassword, hashPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
