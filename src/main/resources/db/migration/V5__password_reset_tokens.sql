CREATE TABLE password_reset_token (
    token_hash VARCHAR(64) NOT NULL,
    user_id BINARY(16) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    consumed_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    INDEX ix_password_reset_user (user_id, expires_at),
    INDEX ix_password_reset_expiry (expires_at)
);
