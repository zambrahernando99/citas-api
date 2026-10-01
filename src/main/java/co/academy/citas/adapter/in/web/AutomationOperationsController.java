package co.academy.citas.adapter.in.web;

import co.academy.citas.application.port.in.AutomationOperationsUseCase;
import co.academy.citas.application.port.in.AutomationOperationsUseCase.DailyAppointments;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Contrato WF-003 consumido por n8n. Protegido por X-Automation-Key y ROLE_AUTOMATION. */
@RestController
@RequestMapping("/api/v1/automation/appointments")
public class AutomationOperationsController {
    private final AutomationOperationsUseCase useCase;
    public AutomationOperationsController(AutomationOperationsUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/daily")
    DailyAppointments daily(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return useCase.dailyAppointments(date);
    }
}
