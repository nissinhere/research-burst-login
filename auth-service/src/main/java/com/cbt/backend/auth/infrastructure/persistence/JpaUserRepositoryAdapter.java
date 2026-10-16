package com.cbt.backend.auth.infrastructure.persistence;

import com.cbt.backend.auth.application.port.out.UserRepository;
import com.cbt.backend.auth.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    public JpaUserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    private User toDomain(UserJpaEntity entity) {
        return User.restore(
                entity.getId(),
                entity.getEmail(),
                entity.getHashPassword(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id(),
                user.email(),
                user.hashPassword(),
                user.isEnabled(),
                user.createdAt(),
                user.updatedAt());
    }
}
