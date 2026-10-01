-- S6 / WF-002: outbox de eventos de cambio de estado hacia n8n (DEC-018).
-- Se escribe en la misma transacción que la transición; un despachador lo envía al webhook con reintentos.
-- No guarda datos personales: el payload se arma al enviar a partir de la cita.
CREATE TABLE automation_event_outbox (
    id BINARY(16) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    new_status VARCHAR(16) NOT NULL,
    appointment_id BINARY(16) NOT NULL,
    reschedule_id BINARY(16) NULL,
    occurred_at TIMESTAMP NOT NULL,
    delivery_status VARCHAR(12) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL,
    last_http_status INT NULL,
    last_error VARCHAR(64) NULL,
    delivered_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_event_outbox_type CHECK (event_type IN ('SPECIALIZED_DECISION', 'RESCHEDULE_DECISION', 'CANCELLATION')),
    CONSTRAINT ck_event_outbox_status CHECK (new_status IN ('APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT ck_event_outbox_delivery CHECK (delivery_status IN ('PENDING', 'DELIVERED', 'FAILED')),
    CONSTRAINT fk_event_outbox_appointment FOREIGN KEY (appointment_id) REFERENCES appointment_record(id),
    CONSTRAINT fk_event_outbox_reschedule FOREIGN KEY (reschedule_id) REFERENCES appointment_reschedule(id)
);

CREATE INDEX ix_event_outbox_due ON automation_event_outbox (delivery_status, next_attempt_at);
