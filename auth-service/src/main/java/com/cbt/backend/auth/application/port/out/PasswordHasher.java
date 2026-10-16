package com.cbt.backend.auth.application.port.out;

public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashPassword);
}
