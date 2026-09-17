package co.academy.citas.adapter.out.security;

import co.academy.citas.application.exception.TokenValidationException;
import co.academy.citas.application.port.out.AccessTokenClaims;
import co.academy.citas.application.port.out.IssuedToken;
import co.academy.citas.application.port.out.JwtTokenPort;
import co.academy.citas.application.port.out.RefreshTokenClaims;
import co.academy.citas.domain.account.Role;
import co.academy.citas.domain.account.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAdapter implements JwtTokenPort {
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenAdapter(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public IssuedToken issueAccess(UserAccount userAccount) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusSeconds(properties.getAccessTtlSeconds());
        String token = Jwts.builder()
                .subject(userAccount.id().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("token_type", ACCESS)
                .claim("roles", userAccount.roles().stream().map(Enum::name).sorted().toList())
                .signWith(signingKey())
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    @Override
    public IssuedToken issueRefresh(UserAccount userAccount, UUID sessionId) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusSeconds(properties.getRefreshTtlSeconds());
        String token = Jwts.builder()
                .id(sessionId.toString())
                .subject(userAccount.id().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("token_type", REFRESH)
                .signWith(signingKey())
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    @Override
    public AccessTokenClaims parseAccess(String token) {
        Claims claims = parse(token, ACCESS);
        try {
            List<?> rawRoles = claims.get("roles", List.class);
            if (rawRoles == null || rawRoles.isEmpty()) {
                throw new IllegalArgumentException();
            }
            Set<Role> roles = rawRoles.stream()
                    .map(Object::toString)
                    .map(Role::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
            return new AccessTokenClaims(UUID.fromString(claims.getSubject()), roles);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new TokenValidationException();
        }
    }

    @Override
    public RefreshTokenClaims parseRefresh(String token) {
        Claims claims = parse(token, REFRESH);
        try {
            return new RefreshTokenClaims(UUID.fromString(claims.getId()), UUID.fromString(claims.getSubject()),
                    claims.getExpiration().toInstant());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new TokenValidationException();
        }
    }

    private Claims parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey())
                    .clock(() -> Date.from(clock.instant()))
                    .build().parseSignedClaims(token).getPayload();
            if (!expectedType.equals(claims.get("token_type", String.class))) {
                throw new TokenValidationException();
            }
            return claims;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new TokenValidationException();
        }
    }

    private SecretKey signingKey() {
        byte[] secret = properties.getSecret() == null ? new byte[0]
                : properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT secret must be configured with at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(secret);
    }
}
