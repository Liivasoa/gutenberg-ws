package mg.msys.gutenber_ws.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.http.HttpStatus;

/**
 * Stateless OAuth2 Resource Server security configuration.
 * <ul>
 *   <li>GET /api/v1/** — requires {@code SCOPE_api:read}</li>
 *   <li>POST /api/v1/batch/** — requires {@code ROLE_ADMIN}</li>
 *   <li>All other requests — authenticated</li>
 *   <li>CSRF disabled (stateless JWT-based API)</li>
 *   <li>Session management — STATELESS</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    public SecurityConfig(KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter) {
        this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection is intentionally disabled: this is a stateless REST API
            // authenticated exclusively via Bearer JWT tokens (no cookies, no browser sessions).
            // Disabling CSRF is the correct and secure choice for this authentication model.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Actuator endpoints (if present)
                .requestMatchers("/actuator/**").permitAll()
                // OpenAPI / Swagger UI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // POST batch requires ADMIN role
                .requestMatchers(HttpMethod.POST, "/api/v1/batch/**").hasRole("ADMIN")
                // All GET API endpoints require api:read scope
                .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAuthority("SCOPE_api:read")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter))
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new AccessDeniedHandlerImpl())
            );

        return http.build();
    }
}
