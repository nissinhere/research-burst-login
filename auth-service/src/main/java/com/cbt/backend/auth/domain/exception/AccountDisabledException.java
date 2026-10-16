package com.cbt.backend.auth.domain.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("Account is disabled");
    }
}
