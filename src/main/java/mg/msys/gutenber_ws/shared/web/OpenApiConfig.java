package mg.msys.gutenber_ws.shared.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration with OAuth2 security scheme backed by Keycloak.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Gutenberg WS API", version = "v1", description = "Read API for Project Gutenberg book catalogue"))
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${app.keycloak.auth-url:http://keycloak:8080/realms/gutenberg/protocol/openid-connect/auth}",
                        tokenUrl = "${app.keycloak.token-url}",
                        scopes = {
                                @OAuthScope(name = "api:read", description = "Read access to Gutenberg API")
                        }
                ),
                clientCredentials = @OAuthFlow(
                        tokenUrl = "${app.keycloak.token-url}",
                        scopes = {
                                @OAuthScope(name = "api:read", description = "Read access to Gutenberg API")
                        }
                )
        )
)
public class OpenApiConfig {

    @Value("${app.keycloak.token-url}")
    private String tokenUrl;

    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("gutenberg-api")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
