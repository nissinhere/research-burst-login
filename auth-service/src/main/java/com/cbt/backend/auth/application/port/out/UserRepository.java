package com.cbt.backend.auth.application.port.out;

import com.cbt.backend.auth.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    User save(User user);
}
