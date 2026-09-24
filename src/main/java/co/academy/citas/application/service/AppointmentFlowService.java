package co.academy.citas.application.service;

import co.academy.citas.application.exception.AppointmentConflictException;
import co.academy.citas.application.exception.AppointmentNotFoundException;
import co.academy.citas.application.exception.InvalidAppointmentException;
import co.academy.citas.application.port.in.AppointmentFlowUseCase;
import co.academy.citas.application.port.out.AppointmentFlowPort;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AppointmentFlowService implements AppointmentFlowUseCase {
    private final AppointmentFlowPort port;
    private final Clock clock;
    public AppointmentFlowService(AppointmentFlowPort port, Clock clock) { this.port = port; this.clock = clock; }

    @Override public void createAvailability(UUID actorId, UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!professionalId.equals(port.professionalForUser(actorId).orElse(null))) throw new InvalidAppointmentException("Only the owning professional can manage availability");
        if (!startsAt.isAfter(LocalDateTime.now(clock)) || !endsAt.isAfter(startsAt) || !startsAt.toLocalDate().equals(endsAt.toLocalDate())
                || startsAt.getMinute() % 30 != 0 || endsAt.getMinute() % 30 != 0 || !port.eligible(professionalId, locationId, -1))
            throw new InvalidAppointmentException("The availability block is invalid");
        port.createBlock(UUID.randomUUID(), professionalId, locationId, startsAt, endsAt);
    }

    @Override @Transactional(readOnly = true) public List<AppointmentFlowPort.Slot> availability(long locationId, long specialtyId, UUID professionalId, LocalDate date) {
        AppointmentFlowPort.SpecialtyDetails specialty = port.specialty(specialtyId).filter(AppointmentFlowPort.SpecialtyDetails::active)
                .orElseThrow(() -> new InvalidAppointmentException("The specialty is not active"));
        if (!port.eligible(professionalId, locationId, specialtyId)) return List.of();
        List<AppointmentFlowPort.Slot> slots = port.availableSlots(locationId, specialtyId, professionalId, date);
        if (specialty.durationMinutes() == 30) return slots;
        return slots.stream().filter(slot -> slots.stream().anyMatch(next -> next.startsAt().equals(slot.endsAt()))).toList();
    }

    @Override public AppointmentFlowPort.Appointment reserve(UUID patientId, ReservationCommand command) {
        AppointmentFlowPort.SpecialtyDetails specialty = port.specialty(command.specialtyId()).filter(AppointmentFlowPort.SpecialtyDetails::active)
                .orElseThrow(() -> new InvalidAppointmentException("The specialty is not active"));
        if (!command.startsAt().isAfter(LocalDateTime.now(clock)) || !port.eligible(command.professionalId(), command.locationId(), command.specialtyId()))
            throw new InvalidAppointmentException("The requested appointment is not eligible");
        LocalDateTime end = command.startsAt().plusMinutes(specialty.durationMinutes());
        UUID id = UUID.randomUUID();
        String status = "MEDICINA_GENERAL".equals(specialty.code()) ? "APPROVED" : "REQUESTED";
        if (!port.reserve(id, patientId, command.professionalId(), command.locationId(), command.specialtyId(), status, command.startsAt(), end, command.reason()))
            throw new AppointmentConflictException();
        return port.findAppointment(id).orElseThrow();
    }

    @Override @Transactional(readOnly = true) public List<AppointmentFlowPort.Appointment> pending() { return port.pendingAppointments(); }

    @Override public AppointmentFlowPort.Appointment decide(UUID adminId, UUID appointmentId, DecisionCommand command) {
        if (!command.approve() && (command.reason() == null || command.reason().isBlank())) throw new InvalidAppointmentException("A rejection reason is required");
        String status = command.approve() ? "APPROVED" : "REJECTED";
        if (!port.decide(appointmentId, adminId, status, command.reason())) throw new AppointmentNotFoundException();
        return port.findAppointment(appointmentId).orElseThrow();
    }
}
