package co.academy.citas.application.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentFlowPort {
    Optional<UUID> professionalForUser(UUID userId);
    boolean eligible(UUID professionalId, long locationId, long specialtyId);
    Optional<SpecialtyDetails> specialty(long specialtyId);
    void createBlock(UUID blockId, UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt);
    List<Slot> availableSlots(long locationId, long specialtyId, UUID professionalId, LocalDate date);
    boolean reserve(UUID appointmentId, UUID patientId, UUID professionalId, long locationId, long specialtyId,
                    String status, LocalDateTime startsAt, LocalDateTime endsAt, String reason);
    List<Appointment> pendingAppointments();
    Optional<Appointment> findAppointment(UUID appointmentId);
    boolean decide(UUID appointmentId, UUID adminId, String status, String reason);
    record SpecialtyDetails(long id, String code, String name, int durationMinutes, boolean active) { }
    record Slot(LocalDateTime startsAt, LocalDateTime endsAt) { }
    record Appointment(UUID id, UUID patientId, UUID professionalId, long locationId, long specialtyId,
                       String specialtyName, String status, LocalDateTime startsAt, LocalDateTime endsAt,
                       String reason, String decisionReason) { }
}
