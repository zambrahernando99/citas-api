package co.academy.citas.application.service;

import co.academy.citas.application.exception.AppointmentConflictException;
import co.academy.citas.application.exception.AppointmentNotFoundException;
import co.academy.citas.application.exception.InvalidAppointmentException;
import co.academy.citas.application.port.in.AppointmentFlowUseCase;
import co.academy.citas.application.port.out.AppointmentFlowPort;
import co.academy.citas.application.port.out.AutomationEventPort;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AppointmentFlowService implements AppointmentFlowUseCase {
    private final AppointmentFlowPort port;
    private final Clock clock;
    private final AutomationEventPort events;
    public AppointmentFlowService(AppointmentFlowPort port, Clock clock, AutomationEventPort events) {
        this.port = port; this.clock = clock; this.events = events;
    }

    @Override public void createAvailability(UUID actorId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) {
        UUID professionalId = port.professionalForUser(actorId).orElseThrow(() -> new InvalidAppointmentException("Professional profile was not found"));
        validateBlock(professionalId, locationId, startsAt, endsAt);
        port.createBlock(UUID.randomUUID(), professionalId, locationId, startsAt, endsAt);
    }

    @Override @Transactional(readOnly = true)
    public List<AppointmentFlowPort.AvailabilityBlock> myAvailability(UUID actorId, LocalDate from, LocalDate to) {
        UUID professionalId = port.professionalForUser(actorId).orElseThrow(() -> new InvalidAppointmentException("Professional profile was not found"));
        if (from != null && to != null && to.isBefore(from)) throw new InvalidAppointmentException("The date range is invalid");
        return port.availabilityBlocks(professionalId, from, to);
    }
    @Override @Transactional(readOnly = true)
    public List<AppointmentFlowPort.Appointment> professionalAppointments(UUID actorId, LocalDate from, LocalDate to, Long locationId) {
        UUID professionalId = port.professionalForUser(actorId).orElseThrow(() -> new InvalidAppointmentException("Professional profile was not found"));
        if (from != null && to != null && to.isBefore(from)) throw new InvalidAppointmentException("The date range is invalid");
        return port.professionalAppointments(professionalId, from, to, locationId);
    }

    @Override public AppointmentFlowPort.AvailabilityBlock updateAvailability(UUID actorId, UUID blockId, long locationId,
                                                                                LocalDateTime startsAt, LocalDateTime endsAt) {
        UUID professionalId = port.professionalForUser(actorId).orElseThrow(() -> new InvalidAppointmentException("Professional profile was not found"));
        validateBlock(professionalId, locationId, startsAt, endsAt);
        if (!port.updateBlock(blockId, professionalId, locationId, startsAt, endsAt))
            throw new InvalidAppointmentException("The block is unavailable, committed, or does not belong to you");
        return port.availabilityBlocks(professionalId, startsAt.toLocalDate(), endsAt.toLocalDate()).stream()
                .filter(value -> value.id().equals(blockId)).findFirst().orElseThrow();
    }

    @Override public void deleteAvailability(UUID actorId, UUID blockId) {
        UUID professionalId = port.professionalForUser(actorId).orElseThrow(() -> new InvalidAppointmentException("Professional profile was not found"));
        if (!port.deleteBlock(blockId, professionalId))
            throw new InvalidAppointmentException("The block is unavailable, committed, or does not belong to you");
    }

    private void validateBlock(UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!startsAt.isAfter(LocalDateTime.now(clock)) || !endsAt.isAfter(startsAt)
                || !startsAt.toLocalDate().equals(endsAt.toLocalDate()) || startsAt.getMinute() % 30 != 0
                || endsAt.getMinute() % 30 != 0 || startsAt.getSecond() != 0 || endsAt.getSecond() != 0
                || startsAt.getNano() != 0 || endsAt.getNano() != 0 || !port.eligible(professionalId, locationId, -1))
            throw new InvalidAppointmentException("The availability block is invalid");
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

    @Override @Transactional(readOnly = true)
    public List<AppointmentFlowPort.Appointment> myAppointments(UUID patientId, String status, LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) throw new InvalidAppointmentException("The date range is invalid");
        return port.userAppointments(patientId, status, from, to);
    }
    @Override @Transactional(readOnly = true)
    public List<AppointmentFlowPort.RescheduleRequest> pendingReschedules() { return port.pendingReschedules(); }

    @Override public AppointmentFlowPort.Appointment decide(UUID adminId, UUID appointmentId, DecisionCommand command) {
        if (!command.approve() && (command.reason() == null || command.reason().isBlank())) throw new InvalidAppointmentException("A rejection reason is required");
        String status = command.approve() ? "APPROVED" : "REJECTED";
        if (!port.decide(appointmentId, adminId, status, command.reason())) throw new AppointmentNotFoundException();
        enqueue("SPECIALIZED_DECISION", status, appointmentId, null);
        return port.findAppointment(appointmentId).orElseThrow();
    }

    @Override public void cancel(UUID patientId, UUID appointmentId, String reason) {
        if (!port.cancel(appointmentId, patientId, reason, LocalDateTime.now(clock)))
            throw new AppointmentNotFoundException();
        enqueue("CANCELLATION", "CANCELLED", appointmentId, null);
    }
    @Override public void closeAppointment(UUID actorId, UUID appointmentId, String status) {
        if (!"COMPLETED".equals(status) && !"NO_SHOW".equals(status))
            throw new InvalidAppointmentException("Only COMPLETED or NO_SHOW can close an appointment");
        UUID professionalId = port.professionalForUser(actorId).orElseThrow(() -> new InvalidAppointmentException("Professional profile was not found"));
        if (!port.closeAppointment(appointmentId, professionalId, status, LocalDateTime.now(clock)))
            throw new AppointmentNotFoundException();
    }

    @Override public AppointmentFlowPort.RescheduleRequest requestReschedule(UUID patientId, UUID appointmentId,
                                                                               LocalDateTime startsAt, String reason) {
        AppointmentFlowPort.Appointment appointment = port.findAppointment(appointmentId)
                .filter(value -> value.patientId().equals(patientId))
                .orElseThrow(AppointmentNotFoundException::new);
        if (!"APPROVED".equals(appointment.status()) || !appointment.startsAt().isAfter(LocalDateTime.now(clock)))
            throw new InvalidAppointmentException("Only an approved future appointment can be rescheduled");
        if (startsAt == null || !startsAt.isAfter(LocalDateTime.now(clock))
                || startsAt.getMinute() % 30 != 0 || startsAt.getSecond() != 0 || startsAt.getNano() != 0)
            throw new InvalidAppointmentException("The requested time is invalid");
        LocalDateTime endsAt = startsAt.plusMinutes(appointment.durationMinutes());
        return port.requestReschedule(UUID.randomUUID(), appointmentId, patientId, startsAt, endsAt, reason)
                .orElseThrow(AppointmentConflictException::new);
    }

    @Override public AppointmentFlowPort.RescheduleRequest decideReschedule(UUID adminId, UUID requestId,
                                                                              DecisionCommand command) {
        if (!command.approve() && (command.reason() == null || command.reason().isBlank()))
            throw new InvalidAppointmentException("A rejection reason is required");
        AppointmentFlowPort.RescheduleRequest decided = port.decideReschedule(requestId, adminId, command.approve(), command.reason())
                .orElseThrow(AppointmentNotFoundException::new);
        enqueue("RESCHEDULE_DECISION", command.approve() ? "APPROVED" : "REJECTED", decided.appointmentId(), requestId);
        return decided;
    }

    @Override @Transactional(readOnly = true)
    public List<AppointmentFlowPort.StatusChange> history(UUID actorId, boolean admin, UUID appointmentId) {
        AppointmentFlowPort.Appointment appointment = port.findAppointment(appointmentId).orElseThrow(AppointmentNotFoundException::new);
        boolean authorized = admin || appointment.patientId().equals(actorId)
                || port.professionalForUser(actorId).filter(appointment.professionalId()::equals).isPresent();
        if (!authorized) throw new AppointmentNotFoundException();
        return port.statusHistory(appointmentId);
    }

    @Override @Transactional(readOnly = true)
    public List<InboxItem> inbox(InboxFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.to().isBefore(filter.from()))
            throw new InvalidAppointmentException("The date range is invalid");
        String type = filter.type() == null || filter.type().isBlank() ? null : filter.type();
        if (type != null && !"APPOINTMENT".equals(type) && !"RESCHEDULE".equals(type))
            throw new InvalidAppointmentException("The inbox type is invalid");
        Stream<InboxItem> requested = port.pendingAppointments().stream()
                .map(value -> new InboxItem("APPOINTMENT", value.id(), value, null));
        Stream<InboxItem> reschedules = port.pendingReschedules().stream()
                .map(value -> new InboxItem("RESCHEDULE", value.id(), port.findAppointment(value.appointmentId()).orElseThrow(), value));
        return Stream.concat(requested, reschedules)
                .filter(item -> type == null || type.equals(item.type()))
                .filter(item -> filter.locationId() == null || item.appointment().locationId() == filter.locationId())
                .filter(item -> filter.professionalId() == null || item.appointment().professionalId().equals(filter.professionalId()))
                .filter(item -> filter.specialtyId() == null || item.appointment().specialtyId() == filter.specialtyId())
                .filter(item -> filter.from() == null || !item.startsAt().toLocalDate().isBefore(filter.from()))
                .filter(item -> filter.to() == null || !item.startsAt().toLocalDate().isAfter(filter.to()))
                .sorted(Comparator.comparing(InboxItem::startsAt))
                .toList();
    }

    // WF-002 (S6): el evento queda en la misma transacción que la transición; si ésta falla, no hay evento
    private void enqueue(String type, String status, UUID appointmentId, UUID rescheduleId) {
        events.enqueue(UUID.randomUUID(), type, status, appointmentId, rescheduleId, LocalDateTime.now(clock));
    }
}
