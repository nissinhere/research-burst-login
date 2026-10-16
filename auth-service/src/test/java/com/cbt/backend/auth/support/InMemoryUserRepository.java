package com.cbt.backend.auth.support;

import com.cbt.backend.auth.application.port.out.UserRepository;
import com.cbt.backend.auth.domain.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> usersByEmail = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public User save(User user) {
        Long id = user.id() != null ? user.id() : sequence.incrementAndGet();
        User saved = User.restore(id, user.email(), user.hashPassword(), user.isEnabled(), user.createdAt(), user.updatedAt());
        usersByEmail.put(saved.email(), saved);
        return saved;
    }
}
