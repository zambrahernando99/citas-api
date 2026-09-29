package co.academy.citas.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetPort {
    Optional<UUID> userIdByEmail(String normalizedEmail);
    void saveToken(String fingerprint, UUID userId, Instant expiresAt);
    Optional<UUID> consumeToken(String fingerprint, Instant consumedAt);
    void updatePassword(UUID userId, String passwordHash);
    void revokeSessions(UUID userId, Instant revokedAt);
}
