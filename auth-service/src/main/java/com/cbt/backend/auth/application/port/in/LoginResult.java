package com.cbt.backend.auth.application.port.in;

public record LoginResult(String accessToken, String tokenType, long expiresInSeconds) {
}
