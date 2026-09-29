CREATE TABLE eps_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_eps_code UNIQUE (code),
    CONSTRAINT uq_eps_name UNIQUE (name)
);

CREATE TABLE eps_plan (
    id BIGINT NOT NULL AUTO_INCREMENT,
    eps_id BIGINT NOT NULL,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_eps_plan_code UNIQUE (eps_id, code),
    CONSTRAINT uq_eps_plan_id_eps UNIQUE (id, eps_id),
    CONSTRAINT fk_eps_plan_eps FOREIGN KEY (eps_id) REFERENCES eps_catalog(id)
);

CREATE TABLE regime_catalog (
    code VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (code),
    CONSTRAINT uq_regime_name UNIQUE (name)
);

CREATE TABLE user_affiliation (
    user_id BINARY(16) NOT NULL,
    eps_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    regime_code VARCHAR(40) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_affiliation_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_user_affiliation_eps FOREIGN KEY (eps_id) REFERENCES eps_catalog(id),
    CONSTRAINT fk_user_affiliation_plan_eps FOREIGN KEY (plan_id, eps_id) REFERENCES eps_plan(id, eps_id),
    CONSTRAINT fk_user_affiliation_regime FOREIGN KEY (regime_code) REFERENCES regime_catalog(code)
);
