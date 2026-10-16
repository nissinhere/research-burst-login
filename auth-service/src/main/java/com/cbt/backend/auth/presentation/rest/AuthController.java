package com.cbt.backend.auth.presentation.rest;

import com.cbt.backend.auth.application.port.in.LoginCommand;
import com.cbt.backend.auth.application.port.in.LoginResult;
import com.cbt.backend.auth.application.port.in.LoginUseCase;
import com.cbt.backend.auth.presentation.rest.dto.LoginRequest;
import com.cbt.backend.auth.presentation.rest.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return new LoginResponse(result.accessToken(), result.tokenType(), result.expiresInSeconds());
    }
}
