package co.academy.citas.application.port.out;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenClaims(UUID sessionId, UUID userId, Instant expiresAt) {
}
