package com.cbt.backend.auth.application.port.in;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}
