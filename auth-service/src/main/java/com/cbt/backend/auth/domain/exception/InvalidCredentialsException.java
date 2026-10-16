package com.cbt.backend.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
