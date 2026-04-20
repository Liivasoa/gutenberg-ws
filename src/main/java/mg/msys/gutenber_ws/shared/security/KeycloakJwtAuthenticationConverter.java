package mg.msys.gutenber_ws.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a Keycloak JWT into a {@link JwtAuthenticationToken} by extracting:
 * <ul>
 *   <li>Realm roles from {@code realm_access.roles} → prefixed with {@code ROLE_}</li>
 *   <li>Scopes from the {@code scope} claim (space-separated) → prefixed with {@code SCOPE_}</li>
 * </ul>
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract realm roles from realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?> roles) {
                roles.stream()
                        .filter(String.class::isInstance)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
            }
        }

        // Extract scopes from space-separated scope claim
        String scopeClaim = jwt.getClaimAsString("scope");
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            List<GrantedAuthority> scopeAuthorities = List.of(scopeClaim.trim().split("\\s+")).stream()
                    .filter(s -> !s.isBlank())
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toList());
            authorities.addAll(scopeAuthorities);
        }

        return authorities;
    }
}
