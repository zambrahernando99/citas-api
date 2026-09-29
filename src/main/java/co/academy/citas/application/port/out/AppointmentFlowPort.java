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
    List<AvailabilityBlock> availabilityBlocks(UUID professionalId, LocalDate from, LocalDate to);
    boolean updateBlock(UUID blockId, UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt);
    boolean deleteBlock(UUID blockId, UUID professionalId);
    List<Slot> availableSlots(long locationId, long specialtyId, UUID professionalId, LocalDate date);
    boolean reserve(UUID appointmentId, UUID patientId, UUID professionalId, long locationId, long specialtyId,
                    String status, LocalDateTime startsAt, LocalDateTime endsAt, String reason);
    List<Appointment> pendingAppointments();
    List<Appointment> userAppointments(UUID patientId, String status, LocalDate from, LocalDate to);
    List<RescheduleRequest> pendingReschedules();
    List<Appointment> professionalAppointments(UUID professionalId, LocalDate from, LocalDate to, Long locationId);
    Optional<Appointment> findAppointment(UUID appointmentId);
    boolean decide(UUID appointmentId, UUID adminId, String status, String reason);
    boolean cancel(UUID appointmentId, UUID patientId, String reason, LocalDateTime now);
    boolean closeAppointment(UUID appointmentId, UUID professionalId, String status, LocalDateTime now);
    Optional<RescheduleRequest> requestReschedule(UUID requestId, UUID appointmentId, UUID patientId,
                                                  LocalDateTime startsAt, LocalDateTime endsAt, String reason);
    Optional<RescheduleRequest> decideReschedule(UUID requestId, UUID adminId, boolean approve, String reason);
    record SpecialtyDetails(long id, String code, String name, int durationMinutes, boolean active) { }
    record Slot(LocalDateTime startsAt, LocalDateTime endsAt) { }
    record AvailabilityBlock(UUID id, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) { }
    record Appointment(UUID id, UUID patientId, UUID professionalId, long locationId, long specialtyId,
                       String specialtyName, String status, LocalDateTime startsAt, LocalDateTime endsAt,
                       String reason, String decisionReason, String professionalName, String locationName, String patientName,
                       boolean reschedulePending) {
        public int durationMinutes() { return (int) java.time.Duration.between(startsAt, endsAt).toMinutes(); }
    }
    record RescheduleRequest(UUID id, UUID appointmentId, String status, LocalDateTime startsAt,
                             LocalDateTime endsAt, String reason, String decisionReason, String patientName,
                             String specialtyName, String professionalName, String locationName,
                             LocalDateTime previousStartsAt) { }
}
