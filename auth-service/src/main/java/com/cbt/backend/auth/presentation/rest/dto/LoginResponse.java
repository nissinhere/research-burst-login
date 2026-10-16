package com.cbt.backend.auth.presentation.rest.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
