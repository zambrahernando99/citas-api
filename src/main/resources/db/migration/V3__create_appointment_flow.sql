CREATE TABLE availability_block (
    id BINARY(16) NOT NULL,
    professional_id BINARY(16) NOT NULL,
    location_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_availability_block_range CHECK (ends_at > starts_at),
    CONSTRAINT fk_availability_block_professional FOREIGN KEY (professional_id) REFERENCES professional_profile(id),
    CONSTRAINT fk_availability_block_location FOREIGN KEY (location_id) REFERENCES clinic_location(id)
);

CREATE TABLE appointment_record (
    id BINARY(16) NOT NULL,
    patient_user_id BINARY(16) NOT NULL,
    professional_id BINARY(16) NOT NULL,
    location_id BIGINT NOT NULL,
    specialty_id BIGINT NOT NULL,
    status_code VARCHAR(16) NOT NULL,
    scheduled_start_at TIMESTAMP NOT NULL,
    scheduled_end_at TIMESTAMP NOT NULL,
    reason VARCHAR(500),
    decision_reason VARCHAR(500),
    decided_by_user_id BINARY(16),
    decided_at TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT ck_appointment_record_range CHECK (scheduled_end_at > scheduled_start_at),
    CONSTRAINT ck_appointment_record_status CHECK (status_code IN ('REQUESTED', 'APPROVED', 'REJECTED')),
    CONSTRAINT fk_appointment_record_patient FOREIGN KEY (patient_user_id) REFERENCES user_account(id),
    CONSTRAINT fk_appointment_record_professional FOREIGN KEY (professional_id) REFERENCES professional_profile(id),
    CONSTRAINT fk_appointment_record_location FOREIGN KEY (location_id) REFERENCES clinic_location(id),
    CONSTRAINT fk_appointment_record_specialty FOREIGN KEY (specialty_id) REFERENCES specialty_catalog(id)
);

CREATE TABLE professional_slot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    availability_block_id BINARY(16) NOT NULL,
    professional_id BINARY(16) NOT NULL,
    location_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    appointment_id BINARY(16),
    PRIMARY KEY (id),
    CONSTRAINT uq_professional_slot_start UNIQUE (professional_id, starts_at),
    CONSTRAINT fk_professional_slot_block FOREIGN KEY (availability_block_id) REFERENCES availability_block(id),
    CONSTRAINT fk_professional_slot_appointment FOREIGN KEY (appointment_id) REFERENCES appointment_record(id)
);

CREATE TABLE appointment_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    appointment_id BINARY(16) NOT NULL,
    status_code VARCHAR(16) NOT NULL,
    changed_by_user_id BINARY(16),
    change_source VARCHAR(16) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_appointment_status_history_appointment FOREIGN KEY (appointment_id) REFERENCES appointment_record(id)
);
