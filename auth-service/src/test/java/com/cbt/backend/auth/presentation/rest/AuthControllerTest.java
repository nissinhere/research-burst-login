package com.cbt.backend.auth.presentation.rest;

import com.cbt.backend.auth.application.port.in.LoginCommand;
import com.cbt.backend.auth.application.port.in.LoginResult;
import com.cbt.backend.auth.application.port.in.LoginUseCase;
import com.cbt.backend.auth.domain.exception.AccountDisabledException;
import com.cbt.backend.auth.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    private MockMvc mvcWith(LoginUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(new AuthController(useCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String body(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    @DisplayName("given valid credentials, when posting login, then 200 with token payload")
    void givenValidCredentials_whenPostingLogin_thenOk() throws Exception {
        AtomicReference<LoginCommand> captured = new AtomicReference<>();
        LoginUseCase useCase = command -> {
            captured.set(command);
            return new LoginResult("jwt-token", "Bearer", 3600);
        };

        mvcWith(useCase).perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("john@example.com", "S3cret-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        assertThat(captured.get()).isEqualTo(new LoginCommand("john@example.com", "S3cret-pass"));
    }

    @Test
    @DisplayName("given invalid credentials, when posting login, then 401")
    void givenInvalidCredentials_whenPostingLogin_thenUnauthorized() throws Exception {
        LoginUseCase useCase = command -> {
            throw new InvalidCredentialsException();
        };

        mvcWith(useCase).perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("john@example.com", "wrong-pass")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("given disabled account, when posting login, then 403")
    void givenDisabledAccount_whenPostingLogin_thenForbidden() throws Exception {
        LoginUseCase useCase = command -> {
            throw new AccountDisabledException();
        };

        mvcWith(useCase).perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("john@example.com", "S3cret-pass")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    @DisplayName("given malformed email, when posting login, then 400 validation error")
    void givenMalformedEmail_whenPostingLogin_thenBadRequest() throws Exception {
        mvcWith(command -> new LoginResult("t", "Bearer", 1)).perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("not-an-email", "S3cret-pass")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("given blank password, when posting login, then 400 validation error")
    void givenBlankPassword_whenPostingLogin_thenBadRequest() throws Exception {
        mvcWith(command -> new LoginResult("t", "Bearer", 1)).perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("john@example.com", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("given malformed json, when posting login, then 400 malformed request")
    void givenMalformedJson_whenPostingLogin_thenBadRequest() throws Exception {
        mvcWith(command -> new LoginResult("t", "Bearer", 1)).perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
