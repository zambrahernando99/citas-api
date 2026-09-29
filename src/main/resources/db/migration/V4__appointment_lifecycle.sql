ALTER TABLE appointment_record
    DROP CONSTRAINT ck_appointment_record_status;

ALTER TABLE appointment_record
    ADD CONSTRAINT ck_appointment_record_status
        CHECK (status_code IN ('REQUESTED', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'));

CREATE TABLE appointment_reschedule (
    id BINARY(16) NOT NULL,
    appointment_id BINARY(16) NOT NULL,
    requested_by_user_id BINARY(16) NOT NULL,
    status_code VARCHAR(16) NOT NULL,
    proposed_start_at TIMESTAMP NOT NULL,
    proposed_end_at TIMESTAMP NOT NULL,
    reason VARCHAR(500),
    decision_reason VARCHAR(500),
    decided_by_user_id BINARY(16),
    decided_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT ck_appointment_reschedule_status CHECK (status_code IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_appointment_reschedule_range CHECK (proposed_end_at > proposed_start_at),
    CONSTRAINT fk_appointment_reschedule_appointment FOREIGN KEY (appointment_id) REFERENCES appointment_record(id),
    CONSTRAINT fk_appointment_reschedule_requester FOREIGN KEY (requested_by_user_id) REFERENCES user_account(id),
    CONSTRAINT fk_appointment_reschedule_decider FOREIGN KEY (decided_by_user_id) REFERENCES user_account(id),
    INDEX ix_appointment_reschedule_status (status_code, created_at),
    INDEX ix_appointment_reschedule_appointment (appointment_id, status_code)
);

ALTER TABLE professional_slot
    ADD COLUMN reschedule_request_id BINARY(16) NULL;

ALTER TABLE professional_slot
    ADD CONSTRAINT fk_professional_slot_reschedule FOREIGN KEY (reschedule_request_id) REFERENCES appointment_reschedule(id);

CREATE INDEX ix_professional_slot_reschedule ON professional_slot (reschedule_request_id);

CREATE INDEX ix_appointment_patient_start ON appointment_record (patient_user_id, scheduled_start_at);
CREATE INDEX ix_appointment_professional_start ON appointment_record (professional_id, scheduled_start_at, status_code);
