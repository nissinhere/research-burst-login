package com.cbt.backend.auth.application.port.in;

public record LoginCommand(String email, String password) {
}
