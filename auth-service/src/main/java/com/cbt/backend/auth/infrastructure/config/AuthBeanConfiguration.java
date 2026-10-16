package com.cbt.backend.auth.infrastructure.config;

import com.cbt.backend.auth.application.port.in.LoginUseCase;
import com.cbt.backend.auth.application.port.out.PasswordHasher;
import com.cbt.backend.auth.application.port.out.TokenIssuer;
import com.cbt.backend.auth.application.port.out.UserRepository;
import com.cbt.backend.auth.application.service.LoginService;
import com.cbt.backend.auth.infrastructure.security.BcryptPasswordHasher;
import com.cbt.backend.auth.infrastructure.security.HmacJwtTokenIssuer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthBeanConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordHasher passwordHasher(AuthProperties properties) {
        return new BcryptPasswordHasher(properties.password().bcryptStrength());
    }

    @Bean
    public TokenIssuer tokenIssuer(AuthProperties properties, Clock clock) {
        return new HmacJwtTokenIssuer(properties.jwt().secret(), properties.jwt().ttlSeconds(), clock);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        return new LoginService(userRepository, passwordHasher, tokenIssuer);
    }
}
