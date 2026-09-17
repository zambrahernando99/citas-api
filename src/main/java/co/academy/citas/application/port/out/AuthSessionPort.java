package co.academy.citas.application.port.out;

import co.academy.citas.domain.session.AuthSession;
import java.time.Instant;
import java.util.UUID;

public interface AuthSessionPort {
    void create(AuthSession session);
    boolean rotate(UUID currentSessionId, String currentTokenFingerprint, Instant now, AuthSession replacement);
    boolean revoke(UUID sessionId, String tokenFingerprint, Instant now);
}
