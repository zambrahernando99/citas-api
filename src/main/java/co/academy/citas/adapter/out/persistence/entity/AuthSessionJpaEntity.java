package co.academy.citas.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auth_session")
public class AuthSessionJpaEntity {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
    private String refreshTokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected AuthSessionJpaEntity() {
    }

    public AuthSessionJpaEntity(UUID id, UUID userId, String refreshTokenHash, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.userId = userId;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void revoke(Instant at) { this.revokedAt = at; }
}
