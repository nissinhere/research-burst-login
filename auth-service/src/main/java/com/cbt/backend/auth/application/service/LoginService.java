package com.cbt.backend.auth.application.service;

import com.cbt.backend.auth.application.port.in.LoginCommand;
import com.cbt.backend.auth.application.port.in.LoginResult;
import com.cbt.backend.auth.application.port.in.LoginUseCase;
import com.cbt.backend.auth.application.port.out.IssuedToken;
import com.cbt.backend.auth.application.port.out.PasswordHasher;
import com.cbt.backend.auth.application.port.out.TokenIssuer;
import com.cbt.backend.auth.application.port.out.UserRepository;
import com.cbt.backend.auth.domain.exception.AccountDisabledException;
import com.cbt.backend.auth.domain.exception.InvalidCredentialsException;
import com.cbt.backend.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public class LoginService implements LoginUseCase {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final String timingGuardHash;

    public LoginService(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.timingGuardHash = passwordHasher.hash(UUID.randomUUID().toString());
    }

    @Override
    public LoginResult login(LoginCommand command) {
        if (command == null || command.email() == null || command.password() == null) {
            throw new InvalidCredentialsException();
        }

        Optional<User> found = userRepository.findByEmail(User.normalizeEmail(command.email()));
        String hashToCheck = found.map(User::hashPassword).orElse(timingGuardHash);
        boolean passwordMatches = passwordHasher.matches(command.password(), hashToCheck);

        if (found.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        User user = found.get();
        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }

        IssuedToken token = tokenIssuer.issue(user);
        return new LoginResult(token.value(), TOKEN_TYPE, token.expiresInSeconds());
    }
}
