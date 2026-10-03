package co.academy.citas.adapter.in.web;

import co.academy.citas.application.port.in.AppointmentReminderUseCase;
import co.academy.citas.application.port.in.AppointmentReminderUseCase.DeliveryCommand;
import co.academy.citas.application.port.in.AppointmentReminderUseCase.DueReminders;
import co.academy.citas.application.port.out.AppointmentReminderPort.Delivery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Contrato S5 consumido por n8n (WF-001). Protegido por X-Automation-Key y ROLE_AUTOMATION. */
@RestController
@RequestMapping("/api/v1/automation/reminders")
public class AutomationReminderController {
    private final AppointmentReminderUseCase useCase;
    public AutomationReminderController(AppointmentReminderUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/due")
    DueReminders due(@RequestParam(required = false) Integer windowHours) { return useCase.findDue(windowHours); }

    @PostMapping("/{appointmentId}/deliveries")
    Delivery recordDelivery(@PathVariable UUID appointmentId, @Valid @RequestBody DeliveryRequest request) {
        return useCase.recordDelivery(appointmentId, new DeliveryCommand(request.windowCode(), request.status(),
                request.channel(), request.providerMessageId(), request.errorCode()));
    }

    record DeliveryRequest(
            @NotBlank @Pattern(regexp = "H[0-9]{1,2}") String windowCode,
            @NotBlank @Pattern(regexp = "SENT|FAILED") String status,
            @Pattern(regexp = "GMAIL") String channel,
            @Size(max = 128) @Pattern(regexp = "[A-Za-z0-9._:@<>+=-]*") String providerMessageId,
            @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_.-]*") String errorCode) {}
}
