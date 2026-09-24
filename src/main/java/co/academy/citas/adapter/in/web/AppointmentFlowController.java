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
        useCase.createAvailability(actor(principal), request.professionalId(), request.locationId(), request.startsAt(), request.endsAt());
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    private UUID actor(Principal principal) { return UUID.fromString(principal.getName()); }
    record AvailabilityRequest(@NotNull UUID professionalId, long locationId, @NotNull LocalDateTime startsAt, @NotNull LocalDateTime endsAt) { }
    record ReservationRequest(@NotNull UUID professionalId, long locationId, long specialtyId, @NotNull LocalDateTime startsAt, String reason) { }
    record DecisionRequest(boolean approve, String reason) { }
    record SlotResponse(LocalDateTime startsAt, LocalDateTime endsAt) { static SlotResponse from(AppointmentFlowPort.Slot slot) { return new SlotResponse(slot.startsAt(), slot.endsAt()); } }
    record AppointmentResponse(UUID id, String status, String specialty, LocalDateTime startsAt, LocalDateTime endsAt, String reason, String decisionReason) {
        static AppointmentResponse from(AppointmentFlowPort.Appointment value) { return new AppointmentResponse(value.id(), value.status(), value.specialtyName(), value.startsAt(), value.endsAt(), value.reason(), value.decisionReason()); }
    }
}
