package com.cbt.backend.auth.application.port.out;

public record IssuedToken(String value, long expiresInSeconds) {
}
