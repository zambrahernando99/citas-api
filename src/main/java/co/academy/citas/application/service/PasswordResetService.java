package co.academy.citas.application.service;

import co.academy.citas.application.exception.InvalidPasswordResetTokenException;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.PasswordResetPort;
import co.academy.citas.application.port.out.TokenFingerprintPort;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final PasswordResetPort resets;
    private final PasswordHashingPort passwords;
    private final TokenFingerprintPort fingerprints;
    private final Clock clock;
    private final Duration ttl;

    public PasswordResetService(PasswordResetPort resets, PasswordHashingPort passwords,
                                TokenFingerprintPort fingerprints, Clock clock, long ttlSeconds) {
        this.resets = resets; this.passwords = passwords; this.fingerprints = fingerprints;
        this.clock = clock; this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public Optional<String> request(String email) {
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return resets.userIdByEmail(email.trim().toLowerCase(Locale.ROOT)).map(userId -> {
            resets.saveToken(fingerprints.fingerprint(token), userId, clock.instant().plus(ttl));
            return token;
        });
    }

    public void reset(String token, String newPassword) {
        var now = clock.instant();
        var userId = resets.consumeToken(fingerprints.fingerprint(token), now)
                .orElseThrow(InvalidPasswordResetTokenException::new);
        resets.updatePassword(userId, passwords.hash(newPassword));
        resets.revokeSessions(userId, now);
    }
}
