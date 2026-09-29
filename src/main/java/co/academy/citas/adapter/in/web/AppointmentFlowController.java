package co.academy.citas.adapter.in.web;

import co.academy.citas.application.port.in.AppointmentFlowUseCase;
import co.academy.citas.application.port.out.AppointmentFlowPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AppointmentFlowController {
    private final AppointmentFlowUseCase useCase;
    public AppointmentFlowController(AppointmentFlowUseCase useCase) { this.useCase = useCase; }

    @PostMapping("/professional/availability-blocks")
    ResponseEntity<Void> createBlock(Principal principal, @Valid @RequestBody AvailabilityRequest request) {
        useCase.createAvailability(actor(principal), request.locationId(), request.startsAt(), request.endsAt());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/professional/availability-blocks")
    List<BlockResponse> myBlocks(Principal principal, @RequestParam(required = false) LocalDate from,
                                 @RequestParam(required = false) LocalDate to) {
        return useCase.myAvailability(actor(principal), from, to).stream().map(BlockResponse::from).toList();
    }
    @GetMapping("/professional/appointments")
    List<AppointmentResponse> professionalAppointments(Principal principal, @RequestParam(required = false) LocalDate from,
                                                       @RequestParam(required = false) LocalDate to,
                                                       @RequestParam(required = false) Long locationId) {
        return useCase.professionalAppointments(actor(principal), from, to, locationId).stream().map(AppointmentResponse::from).toList();
    }
    @org.springframework.web.bind.annotation.PatchMapping("/professional/appointments/{appointmentId}/completion")
    ResponseEntity<Void> closeAppointment(Principal principal, @PathVariable UUID appointmentId,
                                          @Valid @RequestBody CompletionRequest request) {
        useCase.closeAppointment(actor(principal), appointmentId, request.status());
        return ResponseEntity.noContent().build();
    }
    @org.springframework.web.bind.annotation.PutMapping("/professional/availability-blocks/{blockId}")
    BlockResponse updateBlock(Principal principal, @PathVariable UUID blockId, @Valid @RequestBody AvailabilityRequest request) {
        return BlockResponse.from(useCase.updateAvailability(actor(principal), blockId, request.locationId(), request.startsAt(), request.endsAt()));
    }
    @DeleteMapping("/professional/availability-blocks/{blockId}")
    ResponseEntity<Void> deleteBlock(Principal principal, @PathVariable UUID blockId) {
        useCase.deleteAvailability(actor(principal), blockId); return ResponseEntity.noContent().build();
    }
    @GetMapping("/availability")
    List<SlotResponse> availability(@RequestParam long locationId, @RequestParam long specialtyId,
                                    @RequestParam UUID professionalId, @RequestParam LocalDate date) {
        return useCase.availability(locationId, specialtyId, professionalId, date).stream().map(SlotResponse::from).toList();
    }
    @PostMapping("/appointments")
    ResponseEntity<AppointmentResponse> reserve(Principal principal, @Valid @RequestBody ReservationRequest request) {
        var appointment = useCase.reserve(actor(principal), new AppointmentFlowUseCase.ReservationCommand(request.professionalId(), request.locationId(), request.specialtyId(), request.startsAt(), request.reason()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(appointment));
    }
    @GetMapping("/admin/appointments/requested")
    List<AppointmentResponse> pending() { return useCase.pending().stream().map(AppointmentResponse::from).toList(); }
    @PostMapping("/admin/appointments/{appointmentId}/decision")
    AppointmentResponse decide(Principal principal, @PathVariable UUID appointmentId, @Valid @RequestBody DecisionRequest request) {
        return AppointmentResponse.from(useCase.decide(actor(principal), appointmentId, new AppointmentFlowUseCase.DecisionCommand(request.approve(), request.reason())));
    }
    @GetMapping("/appointments/mine")
    List<AppointmentResponse> mine(Principal principal, @RequestParam(required = false) String status,
                                   @RequestParam(required = false) LocalDate from,
                                   @RequestParam(required = false) LocalDate to) {
        return useCase.myAppointments(actor(principal), status, from, to).stream().map(AppointmentResponse::from).toList();
    }
    @DeleteMapping("/appointments/{appointmentId}")
    ResponseEntity<Void> cancel(Principal principal, @PathVariable UUID appointmentId,
                                @RequestBody(required = false) CancellationRequest request) {
        useCase.cancel(actor(principal), appointmentId, request == null ? null : request.reason());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/appointments/{appointmentId}/reschedules")
    ResponseEntity<RescheduleResponse> requestReschedule(Principal principal, @PathVariable UUID appointmentId,
                                                         @Valid @RequestBody RescheduleRequest request) {
        var value = useCase.requestReschedule(actor(principal), appointmentId, request.startsAt(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(RescheduleResponse.from(value));
    }
    @GetMapping("/admin/reschedules/pending")
    List<RescheduleResponse> pendingReschedules() { return useCase.pendingReschedules().stream().map(RescheduleResponse::from).toList(); }
    @PostMapping("/admin/reschedules/{requestId}/decision")
    RescheduleResponse decideReschedule(Principal principal, @PathVariable UUID requestId,
                                        @Valid @RequestBody DecisionRequest request) {
        return RescheduleResponse.from(useCase.decideReschedule(actor(principal), requestId,
                new AppointmentFlowUseCase.DecisionCommand(request.approve(), request.reason())));
    }
    private UUID actor(Principal principal) { return UUID.fromString(principal.getName()); }
    record AvailabilityRequest(long locationId, @NotNull LocalDateTime startsAt, @NotNull LocalDateTime endsAt) { }
    record ReservationRequest(@NotNull UUID professionalId, long locationId, long specialtyId, @NotNull LocalDateTime startsAt, String reason) { }
    record DecisionRequest(boolean approve, String reason) { }
    record CancellationRequest(String reason) { }
    record RescheduleRequest(@NotNull @Future LocalDateTime startsAt, String reason) { }
    record CompletionRequest(@NotBlank String status) { }
    record SlotResponse(LocalDateTime startsAt, LocalDateTime endsAt) { static SlotResponse from(AppointmentFlowPort.Slot slot) { return new SlotResponse(slot.startsAt(), slot.endsAt()); } }
    record BlockResponse(UUID id, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) {
        static BlockResponse from(AppointmentFlowPort.AvailabilityBlock block) { return new BlockResponse(block.id(), block.locationId(), block.startsAt(), block.endsAt()); }
    }
    record AppointmentResponse(UUID id, String status, String specialty, LocalDateTime startsAt, LocalDateTime endsAt,
                               int durationMinutes, String professionalName, String location, String patientName,
                               String reason, String decisionReason, boolean reschedulePending) {
        static AppointmentResponse from(AppointmentFlowPort.Appointment value) { return new AppointmentResponse(value.id(), value.status(), value.specialtyName(), value.startsAt(), value.endsAt(), value.durationMinutes(), value.professionalName(), value.locationName(), value.patientName(), value.reason(), value.decisionReason(), value.reschedulePending()); }
    }
    record RescheduleResponse(UUID id, UUID appointmentId, String status, LocalDateTime startsAt, LocalDateTime endsAt,
                              String reason, String decisionReason, String patientName, String specialty,
                              String professionalName, String location, LocalDateTime previousStartsAt) {
        static RescheduleResponse from(AppointmentFlowPort.RescheduleRequest value) { return new RescheduleResponse(value.id(), value.appointmentId(), value.status(), value.startsAt(), value.endsAt(), value.reason(), value.decisionReason(), value.patientName(), value.specialtyName(), value.professionalName(), value.locationName(), value.previousStartsAt()); }
    }
}
