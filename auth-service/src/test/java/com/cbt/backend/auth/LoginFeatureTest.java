package com.cbt.backend.auth;

import com.cbt.backend.auth.application.port.out.PasswordHasher;
import com.cbt.backend.auth.application.port.out.UserRepository;
import com.cbt.backend.auth.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginFeatureTest {

    private static final String PASSWORD = "S3cret-pass";

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private void givenUser(String email, boolean enabled) {
        Instant now = Instant.now();
        userRepository.save(User.restore(null, email, passwordHasher.hash(PASSWORD), enabled, now, now));
    }

    private HttpResponse<String> whenLogin(String email, String password) throws Exception {
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("given registered enabled user, when logging in with correct password, then 200 and jwt returned")
    void givenEnabledUser_whenLoginWithCorrectPassword_thenOkWithJwt() throws Exception {
        String email = uniqueEmail();
        givenUser(email, true);

        HttpResponse<String> response = whenLogin(email, PASSWORD);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).matches("(?s).*\"accessToken\":\"[^.\"]+\\.[^.\"]+\\.[^.\"]+\".*");
        assertThat(response.body()).contains("\"tokenType\":\"Bearer\"");
        assertThat(response.body()).contains("\"expiresIn\":600");
    }

    @Test
    @DisplayName("given registered user, when logging in with wrong password, then 401")
    void givenEnabledUser_whenLoginWithWrongPassword_thenUnauthorized() throws Exception {
        String email = uniqueEmail();
        givenUser(email, true);

        HttpResponse<String> response = whenLogin(email, "wrong-pass");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("given unknown email, when logging in, then 401 with the same error as wrong password")
    void givenUnknownEmail_whenLogin_thenUnauthorized() throws Exception {
        HttpResponse<String> response = whenLogin(uniqueEmail(), PASSWORD);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("given disabled user, when logging in with correct password, then 403")
    void givenDisabledUser_whenLogin_thenForbidden() throws Exception {
        String email = uniqueEmail();
        givenUser(email, false);

        HttpResponse<String> response = whenLogin(email, PASSWORD);

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.body()).contains("ACCOUNT_DISABLED");
    }

    @Test
    @DisplayName("given malformed email, when logging in, then 400")
    void givenMalformedEmail_whenLogin_thenBadRequest() throws Exception {
        HttpResponse<String> response = whenLogin("not-an-email", PASSWORD);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("VALIDATION_ERROR");
    }
}
