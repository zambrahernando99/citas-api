package co.academy.citas.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Outbox de eventos WF-002 (S6) y lectura de citas del día para WF-003. */
public interface AutomationEventPort {
    record PendingEvent(UUID eventId, String eventType, String newStatus, LocalDateTime occurredAt, int attemptCount,
                        UUID appointmentId, LocalDateTime startsAt, LocalDateTime endsAt, String locationName, String specialtyName,
                        String professionalName, String patientFirstName, String patientEmail, String decisionReason) {}

    record DailyAppointment(UUID appointmentId, LocalDateTime startsAt, String status, String locationName, String specialtyName) {}

    void enqueue(UUID eventId, String eventType, String newStatus, UUID appointmentId, UUID rescheduleId, LocalDateTime occurredAt);
    List<PendingEvent> findDispatchable(LocalDateTime now, int limit);
    void markDelivered(UUID eventId, int attemptCount, int httpStatus, LocalDateTime deliveredAt);
    void markRetry(UUID eventId, int attemptCount, Integer httpStatus, String error, LocalDateTime nextAttemptAt);
    void markFailed(UUID eventId, int attemptCount, Integer httpStatus, String error);
    List<DailyAppointment> appointmentsBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
