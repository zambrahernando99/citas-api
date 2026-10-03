package co.academy.citas.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistencia de recordatorios WF-001 (S5). Solo expone datos mínimos para el correo (DEC-016). */
public interface AppointmentReminderPort {
    record DueReminder(UUID appointmentId, LocalDateTime startsAt, LocalDateTime endsAt, String locationName,
                       String specialtyName, String professionalName, String patientFirstName, String patientEmail) {}

    record Delivery(UUID appointmentId, String windowCode, String channel, String status, int attemptCount,
                    String providerMessageId, String errorCode) {}

    List<DueReminder> findDue(LocalDateTime fromExclusive, LocalDateTime toInclusive, String windowCode, int maxAttempts, int limit);
    Optional<String> appointmentStatus(UUID appointmentId);
    Optional<Delivery> findDelivery(UUID appointmentId, String windowCode);
    /** Inserta el primer intento; devuelve false si ya existía un registro para la cita y ventana. */
    boolean insertDelivery(Delivery delivery);
    void updateDelivery(Delivery delivery);
}
