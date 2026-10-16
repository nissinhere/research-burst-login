package com.cbt.backend.auth.application.service;

import com.cbt.backend.auth.application.port.in.LoginCommand;
import com.cbt.backend.auth.application.port.in.LoginResult;
import com.cbt.backend.auth.domain.exception.AccountDisabledException;
import com.cbt.backend.auth.domain.exception.InvalidCredentialsException;
import com.cbt.backend.auth.domain.model.User;
import com.cbt.backend.auth.support.FakePasswordHasher;
import com.cbt.backend.auth.support.FakeTokenIssuer;
import com.cbt.backend.auth.support.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "S3cret-pass";

    private InMemoryUserRepository userRepository;
    private FakePasswordHasher passwordHasher;
    private FakeTokenIssuer tokenIssuer;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        passwordHasher = new FakePasswordHasher();
        tokenIssuer = new FakeTokenIssuer();
        loginService = new LoginService(userRepository, passwordHasher, tokenIssuer);
        passwordHasher.resetMatchCalls();
    }

    private User givenEnabledUser() {
        return userRepository.save(User.register(EMAIL, passwordHasher.hash(PASSWORD), NOW));
    }

    private User givenDisabledUser() {
        return userRepository.save(User.restore(null, EMAIL, passwordHasher.hash(PASSWORD), false, NOW, NOW));
    }

    @Test
    @DisplayName("given valid credentials, when login, then bearer token is returned")
    void givenValidCredentials_whenLogin_thenTokenReturned() {
        User user = givenEnabledUser();

        LoginResult result = loginService.login(new LoginCommand(EMAIL, PASSWORD));

        assertThat(result.accessToken()).isEqualTo("token-for-" + user.id());
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresInSeconds()).isEqualTo(FakeTokenIssuer.TTL_SECONDS);
    }

    @Test
    @DisplayName("given email with different case and spaces, when login, then it still succeeds")
    void givenEmailWithDifferentCaseAndSpaces_whenLogin_thenSucceeds() {
        givenEnabledUser();

        LoginResult result = loginService.login(new LoginCommand("  JOHN@Example.com ", PASSWORD));

        assertThat(result.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("given wrong password, when login, then invalid credentials and no token issued")
    void givenWrongPassword_whenLogin_thenInvalidCredentials() {
        givenEnabledUser();

        assertThatThrownBy(() -> loginService.login(new LoginCommand(EMAIL, "wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(tokenIssuer.issuedCount()).isZero();
    }

    @Test
    @DisplayName("given unknown email, when login, then invalid credentials and no token issued")
    void givenUnknownEmail_whenLogin_thenInvalidCredentials() {
        assertThatThrownBy(() -> loginService.login(new LoginCommand("ghost@example.com", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(tokenIssuer.issuedCount()).isZero();
    }

    @Test
    @DisplayName("given unknown email, when login, then a password check still runs to equalize timing")
    void givenUnknownEmail_whenLogin_thenPasswordCheckStillRuns() {
        assertThatThrownBy(() -> loginService.login(new LoginCommand("ghost@example.com", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(passwordHasher.matchCalls()).isEqualTo(1);
    }

    @Test
    @DisplayName("given disabled user and correct password, when login, then account disabled")
    void givenDisabledUserAndCorrectPassword_whenLogin_thenAccountDisabled() {
        givenDisabledUser();

        assertThatThrownBy(() -> loginService.login(new LoginCommand(EMAIL, PASSWORD)))
                .isInstanceOf(AccountDisabledException.class);
        assertThat(tokenIssuer.issuedCount()).isZero();
    }

    @Test
    @DisplayName("given disabled user and wrong password, when login, then invalid credentials")
    void givenDisabledUserAndWrongPassword_whenLogin_thenInvalidCredentials() {
        givenDisabledUser();

        assertThatThrownBy(() -> loginService.login(new LoginCommand(EMAIL, "wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("given null password, when login, then invalid credentials")
    void givenNullPassword_whenLogin_thenInvalidCredentials() {
        givenEnabledUser();

        assertThatThrownBy(() -> loginService.login(new LoginCommand(EMAIL, null)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("given null email, when login, then invalid credentials")
    void givenNullEmail_whenLogin_thenInvalidCredentials() {
        assertThatThrownBy(() -> loginService.login(new LoginCommand(null, PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
