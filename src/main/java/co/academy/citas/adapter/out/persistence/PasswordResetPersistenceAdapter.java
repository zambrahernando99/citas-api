package co.academy.citas.adapter.out.persistence;

import co.academy.citas.application.port.out.PasswordResetPort;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetPersistenceAdapter implements PasswordResetPort {
    private final JdbcTemplate jdbc;
    public PasswordResetPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<UUID> userIdByEmail(String email) {
        List<byte[]> result = jdbc.query("select id from user_account where email = ?", (rs, row) -> rs.getBytes(1), email);
        return result.stream().findFirst().map(PasswordResetPersistenceAdapter::uuid);
    }
    @Override public void saveToken(String fingerprint, UUID userId, Instant expiresAt) {
        jdbc.update("insert into password_reset_token (token_hash, user_id, expires_at) values (?, ?, ?)", fingerprint, bytes(userId), expiresAt);
    }
    @Override public Optional<UUID> consumeToken(String fingerprint, Instant consumedAt) {
        int updated = jdbc.update("update password_reset_token set consumed_at = ? where token_hash = ? and consumed_at is null and expires_at > ?",
                consumedAt, fingerprint, consumedAt);
        if (updated != 1) return Optional.empty();
        List<byte[]> userIds = jdbc.query("select user_id from password_reset_token where token_hash = ?", (rs, row) -> rs.getBytes(1), fingerprint);
        return userIds.stream().findFirst().map(PasswordResetPersistenceAdapter::uuid);
    }
    @Override public void updatePassword(UUID userId, String passwordHash) {
        jdbc.update("update user_account set password_hash = ? where id = ?", passwordHash, bytes(userId));
    }
    @Override public void revokeSessions(UUID userId, Instant revokedAt) {
        jdbc.update("update auth_session set revoked_at = ? where user_id = ? and revoked_at is null", revokedAt, bytes(userId));
    }
    private static byte[] bytes(UUID value) { return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array(); }
    private static UUID uuid(byte[] value) { ByteBuffer buffer = ByteBuffer.wrap(value); return new UUID(buffer.getLong(), buffer.getLong()); }
}
