package co.academy.citas.application.service;

import co.academy.citas.application.exception.InvalidCredentialsException;
import co.academy.citas.application.exception.InvalidRefreshTokenException;
import co.academy.citas.application.exception.TokenValidationException;
import co.academy.citas.application.port.in.LoginCommand;
import co.academy.citas.application.port.in.LogoutCommand;
import co.academy.citas.application.port.in.RefreshSessionCommand;
import co.academy.citas.application.port.in.SessionUseCase;
import co.academy.citas.application.port.in.TokenPair;
import co.academy.citas.application.port.out.AuthSessionPort;
import co.academy.citas.application.port.out.IssuedToken;
import co.academy.citas.application.port.out.JwtTokenPort;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.RefreshTokenClaims;
import co.academy.citas.application.port.out.TokenFingerprintPort;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.domain.account.UserAccount;
import co.academy.citas.domain.session.AuthSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public class AuthenticationService implements SessionUseCase {
    private final UserAccountPort userAccountPort;
    private final PasswordHashingPort passwordHashingPort;
    private final JwtTokenPort jwtTokenPort;
    private final TokenFingerprintPort tokenFingerprintPort;
    private final AuthSessionPort authSessionPort;
    private final Clock clock;

    public AuthenticationService(
            UserAccountPort userAccountPort,
            PasswordHashingPort passwordHashingPort,
            JwtTokenPort jwtTokenPort,
            TokenFingerprintPort tokenFingerprintPort,
            AuthSessionPort authSessionPort,
            Clock clock) {
        this.userAccountPort = userAccountPort;
        this.passwordHashingPort = passwordHashingPort;
        this.jwtTokenPort = jwtTokenPort;
        this.tokenFingerprintPort = tokenFingerprintPort;
        this.authSessionPort = authSessionPort;
        this.clock = clock;
    }

    @Override
    public TokenPair login(LoginCommand command) {
        UserAccount user = userAccountPort.findByEmail(command.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordHashingPort.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return createSession(user);
    }

    @Override
    public TokenPair refresh(RefreshSessionCommand command) {
        RefreshTokenClaims claims = parseRefresh(command.refreshToken());
        UserAccount user = userAccountPort.findById(claims.userId())
                .orElseThrow(InvalidRefreshTokenException::new);
        TokenPair replacement = issuePair(user, UUID.randomUUID());
        Instant now = clock.instant();
        AuthSession replacementSession = new AuthSession(
                sessionId(replacement.refreshToken()),
                user.id(),
                tokenFingerprintPort.fingerprint(replacement.refreshToken()),
                parseRefresh(replacement.refreshToken()).expiresAt(),
                null);
        if (!authSessionPort.rotate(claims.sessionId(), tokenFingerprintPort.fingerprint(command.refreshToken()), now, replacementSession)) {
            throw new InvalidRefreshTokenException();
        }
        return replacement;
    }

    @Override
    public void logout(LogoutCommand command) {
        RefreshTokenClaims claims = parseRefresh(command.refreshToken());
        if (!authSessionPort.revoke(claims.sessionId(), tokenFingerprintPort.fingerprint(command.refreshToken()), clock.instant())) {
            throw new InvalidRefreshTokenException();
        }
    }

    private TokenPair createSession(UserAccount user) {
        UUID sessionId = UUID.randomUUID();
        TokenPair pair = issuePair(user, sessionId);
        authSessionPort.create(new AuthSession(
                sessionId,
                user.id(),
                tokenFingerprintPort.fingerprint(pair.refreshToken()),
                parseRefresh(pair.refreshToken()).expiresAt(),
                null));
        return pair;
    }

    private TokenPair issuePair(UserAccount user, UUID sessionId) {
        IssuedToken access = jwtTokenPort.issueAccess(user);
        IssuedToken refresh = jwtTokenPort.issueRefresh(user, sessionId);
        Instant now = clock.instant();
        return new TokenPair(
                access.value(),
                refresh.value(),
                Math.max(0, Duration.between(now, access.expiresAt()).toSeconds()),
                Math.max(0, Duration.between(now, refresh.expiresAt()).toSeconds()));
    }

    private RefreshTokenClaims parseRefresh(String token) {
        try {
            return jwtTokenPort.parseRefresh(token);
        } catch (TokenValidationException exception) {
            throw new InvalidRefreshTokenException();
        }
    }

    private UUID sessionId(String refreshToken) {
        return parseRefresh(refreshToken).sessionId();
    }
}
