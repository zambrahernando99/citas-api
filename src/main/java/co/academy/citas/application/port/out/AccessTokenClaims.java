package co.academy.citas.application.port.out;

import co.academy.citas.domain.account.Role;
import java.util.Set;
import java.util.UUID;

public record AccessTokenClaims(UUID userId, Set<Role> roles) {
}
