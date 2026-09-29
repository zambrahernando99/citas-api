package co.academy.citas.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.academy.citas.application.exception.TokenValidationException;
import co.academy.citas.application.port.out.IssuedToken;
import co.academy.citas.domain.account.Role;
import co.academy.citas.domain.account.UserAccount;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenAdapterTest {
    @Test
    void separatesTokenTypesCarriesRolesAndRejectsExpiredRefresh() {
        Instant issuedAt = Instant.parse("2026-09-17T00:00:00Z");
        JwtProperties properties = properties();
        UserAccount user = new UserAccount(UUID.randomUUID(), "Ana", "Prueba", "CC", "100",
                "ana@example.test", "3000000000", "hash", Set.of(Role.USER));
        JwtTokenAdapter issuer = new JwtTokenAdapter(properties, Clock.fixed(issuedAt, ZoneOffset.UTC));
        IssuedToken access = issuer.issueAccess(user);
        IssuedToken refresh = issuer.issueRefresh(user, UUID.randomUUID());

        assertThat(access.value()).isNotEqualTo(refresh.value());
        assertThat(issuer.parseAccess(access.value()).roles()).containsExactly(Role.USER);
        assertThatThrownBy(() -> issuer.parseAccess(refresh.value())).isInstanceOf(TokenValidationException.class);

        JwtTokenAdapter afterExpiry = new JwtTokenAdapter(properties,
                Clock.fixed(issuedAt.plusSeconds(604801), ZoneOffset.UTC));
        assertThatThrownBy(() -> afterExpiry.parseRefresh(refresh.value())).isInstanceOf(TokenValidationException.class);
    }

    private JwtProperties properties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("a".repeat(32));
        properties.setAccessTtlSeconds(900);
        properties.setRefreshTtlSeconds(604800);
        return properties;
    }
}
