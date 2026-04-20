package mg.msys.gutenber_ws.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security integration tests verifying that the OAuth2 Resource Server security rules
 * are correctly enforced at the HTTP layer.
 *
 * <p>Uses a real Spring context backed by a Testcontainers PostgreSQL container.
 * The JWT decoder is configured via {@code test-public-key.pem} (application-test.properties),
 * so no live Keycloak instance is needed. The {@code jwt()} post-processor sets up the
 * SecurityContext directly, bypassing the JWT decoder entirely.
 */
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@Testcontainers
class SecurityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1-alpine");

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    // -----------------------------------------------------------------------
    // Scenario 1: Unauthenticated request → 401
    // -----------------------------------------------------------------------

    @Test
    void getLanguage_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/language"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Scenario 2: Request with api:read scope → 200
    // -----------------------------------------------------------------------

    @Test
    void getLanguage_withApiReadScope_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/language")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_api:read"))))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // Scenario 3: POST batch without ADMIN role (only api:read scope) → 403
    // -----------------------------------------------------------------------

    @Test
    void postBatch_withApiReadScopeOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/batch/jobs/import-books/executions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_api:read"))))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Scenario 4: POST batch with ADMIN role → not 401 or 403
    //   The batch job itself may fail (500) because there is no actual catalog
    //   to download in the test environment — that is acceptable.
    // -----------------------------------------------------------------------

    @Test
    void postBatch_withAdminRole_isNotRejectedBySecurity() throws Exception {
        mockMvc.perform(post("/api/v1/batch/jobs/import-books/executions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus())
                                .as("Response status should not be 401 or 403")
                                .isNotIn(401, 403));
    }
}
