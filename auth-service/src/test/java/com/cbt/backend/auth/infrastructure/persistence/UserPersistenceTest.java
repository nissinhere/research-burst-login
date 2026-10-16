package com.cbt.backend.auth.infrastructure.persistence;

import com.cbt.backend.auth.application.port.out.UserRepository;
import com.cbt.backend.auth.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("given migrations applied, then users table has the expected columns")
    void givenMigrationsApplied_thenUsersTableHasExpectedColumns() throws Exception {
        Set<String> columns = new HashSet<>();
        try (var connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData().getColumns(null, null, "%", "%")) {
            while (rs.next()) {
                if ("users".equalsIgnoreCase(rs.getString("TABLE_NAME"))
                        && "public".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
        }

        assertThat(columns).containsExactlyInAnyOrder(
                "id", "email", "hash_password", "enabled", "created_at", "updated_at");
    }

    @Test
    @DisplayName("given new user, when saving, then id is generated and fields are persisted")
    void givenNewUser_whenSaving_thenIdGeneratedAndFieldsPersisted() {
        User saved = userRepository.save(User.register("save@example.com", "hash", NOW));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.email()).isEqualTo("save@example.com");
        assertThat(saved.hashPassword()).isEqualTo("hash");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.createdAt()).isEqualTo(NOW);
        assertThat(saved.updatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("given stored user, when finding by email, then user is returned")
    void givenStoredUser_whenFindingByEmail_thenReturned() {
        User saved = userRepository.save(User.register("find@example.com", "hash", NOW));

        assertThat(userRepository.findByEmail("find@example.com"))
                .get()
                .extracting(User::id)
                .isEqualTo(saved.id());
    }

    @Test
    @DisplayName("given unknown email, when finding by email, then empty")
    void givenUnknownEmail_whenFindingByEmail_thenEmpty() {
        assertThat(userRepository.findByEmail("ghost@example.com")).isEmpty();
    }

    @Test
    @DisplayName("given disabled user, when saving, then disabled flag is persisted")
    void givenDisabledUser_whenSaving_thenFlagPersisted() {
        userRepository.save(User.restore(null, "off@example.com", "hash", false, NOW, NOW));

        assertThat(userRepository.findByEmail("off@example.com"))
                .get()
                .extracting(User::isEnabled)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("given existing email, when saving another user with same email, then unique constraint fails")
    void givenExistingEmail_whenSavingDuplicate_thenConstraintFails() {
        userRepository.save(User.register("dup@example.com", "hash", NOW));

        assertThatThrownBy(() -> userRepository.save(User.register("dup@example.com", "other", NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
