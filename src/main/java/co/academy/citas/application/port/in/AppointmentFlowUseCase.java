package co.academy.citas.application.port.in;

import co.academy.citas.application.port.out.AppointmentFlowPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentFlowUseCase {
    void createAvailability(UUID actorId, UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt);
    List<AppointmentFlowPort.Slot> availability(long locationId, long specialtyId, UUID professionalId, LocalDate date);
    AppointmentFlowPort.Appointment reserve(UUID patientId, ReservationCommand command);
    List<AppointmentFlowPort.Appointment> pending();
    AppointmentFlowPort.Appointment decide(UUID adminId, UUID appointmentId, DecisionCommand command);
    record ReservationCommand(UUID professionalId, long locationId, long specialtyId, LocalDateTime startsAt, String reason) { }
    record DecisionCommand(boolean approve, String reason) { }
}
