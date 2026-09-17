package co.academy.citas.adapter.out.persistence;

import co.academy.citas.adapter.out.persistence.entity.AuthSessionJpaEntity;
import co.academy.citas.adapter.out.persistence.repository.SpringDataAuthSessionRepository;
import co.academy.citas.application.port.out.AuthSessionPort;
import co.academy.citas.domain.session.AuthSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthSessionPersistenceAdapter implements AuthSessionPort {
    private final SpringDataAuthSessionRepository sessionRepository;

    public AuthSessionPersistenceAdapter(SpringDataAuthSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void create(AuthSession session) {
        sessionRepository.save(toEntity(session));
    }

    @Override
    @Transactional
    public boolean rotate(UUID currentSessionId, String currentTokenFingerprint, Instant now, AuthSession replacement) {
        return sessionRepository.findByIdForUpdate(currentSessionId)
                .filter(session -> isActiveMatch(session, currentTokenFingerprint, now))
                .map(session -> {
                    session.revoke(now);
                    sessionRepository.save(toEntity(replacement));
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean revoke(UUID sessionId, String tokenFingerprint, Instant now) {
        return sessionRepository.findByIdForUpdate(sessionId)
                .filter(session -> isActiveMatch(session, tokenFingerprint, now))
                .map(session -> {
                    session.revoke(now);
                    return true;
                })
                .orElse(false);
    }

    private boolean isActiveMatch(AuthSessionJpaEntity session, String fingerprint, Instant now) {
        return session.getRevokedAt() == null
                && session.getExpiresAt().isAfter(now)
                && MessageDigest.isEqual(session.getRefreshTokenHash().getBytes(StandardCharsets.US_ASCII),
                fingerprint.getBytes(StandardCharsets.US_ASCII));
    }

    private AuthSessionJpaEntity toEntity(AuthSession session) {
        return new AuthSessionJpaEntity(session.id(), session.userId(), session.refreshTokenHash(),
                session.expiresAt(), session.revokedAt());
    }
}
