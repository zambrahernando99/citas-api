package co.academy.citas.domain.session;

import java.time.Instant;
import java.util.UUID;

public record AuthSession(
        UUID id,
        UUID userId,
        String refreshTokenHash,
        Instant expiresAt,
        Instant revokedAt) {
}
