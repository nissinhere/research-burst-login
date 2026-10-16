package com.cbt.backend.auth.support;

import com.cbt.backend.auth.application.port.out.PasswordHasher;

public class FakePasswordHasher implements PasswordHasher {

    private static final String PREFIX = "hashed:";

    private int matchCalls;

    @Override
    public String hash(String rawPassword) {
        return PREFIX + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String hashPassword) {
        matchCalls++;
        return hashPassword.equals(PREFIX + rawPassword);
    }

    public int matchCalls() {
        return matchCalls;
    }

    public void resetMatchCalls() {
        matchCalls = 0;
    }
}
