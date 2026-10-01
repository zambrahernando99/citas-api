-- S5 / WF-001: resultado de entrega de recordatorios por cita y ventana (DEC-014).
-- No guarda email ni cuerpo del mensaje: solo el estado necesario para idempotencia y reintentos.
CREATE TABLE appointment_reminder_delivery (
    id BIGINT NOT NULL AUTO_INCREMENT,
    appointment_id BINARY(16) NOT NULL,
    window_code VARCHAR(8) NOT NULL,
    channel_code VARCHAR(16) NOT NULL,
    status_code VARCHAR(8) NOT NULL,
    attempt_count INT NOT NULL,
    provider_message_id VARCHAR(128) NULL,
    error_code VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_reminder_delivery_window UNIQUE (appointment_id, window_code),
    CONSTRAINT ck_reminder_delivery_status CHECK (status_code IN ('SENT', 'FAILED')),
    CONSTRAINT ck_reminder_delivery_channel CHECK (channel_code IN ('GMAIL')),
    CONSTRAINT ck_reminder_delivery_attempts CHECK (attempt_count >= 1),
    CONSTRAINT fk_reminder_delivery_appointment FOREIGN KEY (appointment_id) REFERENCES appointment_record(id)
);

CREATE INDEX ix_reminder_delivery_status ON appointment_reminder_delivery (window_code, status_code);
