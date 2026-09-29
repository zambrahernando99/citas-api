INSERT INTO role_catalog (code) VALUES ('PROFESSIONAL'), ('ADMIN');

CREATE TABLE clinic_location (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(160) NOT NULL,
    address VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_clinic_location_code UNIQUE (code)
);

CREATE TABLE specialty_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    duration_minutes SMALLINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_specialty_catalog_code UNIQUE (code),
    CONSTRAINT uq_specialty_catalog_name UNIQUE (name),
    CONSTRAINT ck_s3_specialty_catalog_duration CHECK (duration_minutes IN (30, 60))
);

CREATE TABLE professional_profile (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    professional_code VARCHAR(40) NOT NULL,
    license_number VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_professional_profile_user UNIQUE (user_id),
    CONSTRAINT uq_professional_profile_code UNIQUE (professional_code),
    CONSTRAINT uq_professional_profile_license UNIQUE (license_number),
    CONSTRAINT fk_professional_profile_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE professional_specialty (
    professional_id BINARY(16) NOT NULL,
    specialty_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (professional_id, specialty_id),
    CONSTRAINT fk_professional_specialty_profile FOREIGN KEY (professional_id) REFERENCES professional_profile (id),
    CONSTRAINT fk_professional_specialty_catalog FOREIGN KEY (specialty_id) REFERENCES specialty_catalog (id)
);

CREATE TABLE professional_location (
    professional_id BINARY(16) NOT NULL,
    location_id BIGINT NOT NULL,
    PRIMARY KEY (professional_id, location_id),
    CONSTRAINT fk_professional_location_profile FOREIGN KEY (professional_id) REFERENCES professional_profile (id),
    CONSTRAINT fk_professional_location_catalog FOREIGN KEY (location_id) REFERENCES clinic_location (id)
);

INSERT INTO clinic_location (code, name, address) VALUES
    ('HIC', 'Sede HIC', 'Dirección de laboratorio HIC'),
    ('ICV', 'Sede ICV', 'Dirección de laboratorio ICV');

INSERT INTO specialty_catalog (code, name, duration_minutes, active) VALUES
    ('MEDICINA_GENERAL', 'Medicina General', 30, TRUE),
    ('CARDIOLOGIA', 'Cardiología', 60, TRUE),
    ('PEDIATRIA', 'Pediatría', 30, TRUE);
