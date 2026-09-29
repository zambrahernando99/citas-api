package co.academy.citas.application.port.in;

import co.academy.citas.application.port.out.AppointmentFlowPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentFlowUseCase {
    void createAvailability(UUID actorId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt);
    List<AppointmentFlowPort.AvailabilityBlock> myAvailability(UUID actorId, LocalDate from, LocalDate to);
    List<AppointmentFlowPort.Appointment> professionalAppointments(UUID actorId, LocalDate from, LocalDate to, Long locationId);
    AppointmentFlowPort.AvailabilityBlock updateAvailability(UUID actorId, UUID blockId, long locationId,
                                                              LocalDateTime startsAt, LocalDateTime endsAt);
    void deleteAvailability(UUID actorId, UUID blockId);
    List<AppointmentFlowPort.Slot> availability(long locationId, long specialtyId, UUID professionalId, LocalDate date);
    AppointmentFlowPort.Appointment reserve(UUID patientId, ReservationCommand command);
    List<AppointmentFlowPort.Appointment> pending();
    List<AppointmentFlowPort.Appointment> myAppointments(UUID patientId, String status, LocalDate from, LocalDate to);
    List<AppointmentFlowPort.RescheduleRequest> pendingReschedules();
    AppointmentFlowPort.Appointment decide(UUID adminId, UUID appointmentId, DecisionCommand command);
    void cancel(UUID patientId, UUID appointmentId, String reason);
    void closeAppointment(UUID actorId, UUID appointmentId, String status);
    AppointmentFlowPort.RescheduleRequest requestReschedule(UUID patientId, UUID appointmentId,
                                                              LocalDateTime startsAt, String reason);
    AppointmentFlowPort.RescheduleRequest decideReschedule(UUID adminId, UUID requestId, DecisionCommand command);
    record ReservationCommand(UUID professionalId, long locationId, long specialtyId, LocalDateTime startsAt, String reason) { }
    record DecisionCommand(boolean approve, String reason) { }
}
