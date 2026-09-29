CREATE TABLE role_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_role_catalog_code UNIQUE (code)
);

CREATE TABLE user_account (
    id BINARY(16) NOT NULL,
    given_names VARCHAR(255) NOT NULL,
    family_names VARCHAR(255) NOT NULL,
    document_type VARCHAR(64) NOT NULL,
    document_number VARCHAR(128) NOT NULL,
    email VARCHAR(320) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_account_email UNIQUE (email),
    CONSTRAINT uq_user_account_document UNIQUE (document_number)
);

CREATE TABLE user_account_role (
    user_id BINARY(16) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_account_role_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_user_account_role_role FOREIGN KEY (role_id) REFERENCES role_catalog (id)
);

CREATE TABLE auth_session (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_auth_session_token_hash UNIQUE (refresh_token_hash),
    CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    INDEX ix_auth_session_user_id (user_id),
    INDEX ix_auth_session_expires_at (expires_at)
);

INSERT INTO role_catalog (code) VALUES ('USER');
