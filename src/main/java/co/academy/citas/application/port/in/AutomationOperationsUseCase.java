package co.academy.citas.application.port.in;

import co.academy.citas.application.port.out.AutomationEventPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AutomationOperationsUseCase {
    record DispatchResult(int delivered, int retried, int failed) {}

    record DailyAppointments(LocalDate date, String timezone, LocalDateTime generatedAt, List<AutomationEventPort.DailyAppointment> items) {}

    /** WF-002: envía al webhook los eventos pendientes cuyo próximo intento ya venció. */
    DispatchResult dispatchPendingEvents();

    /** WF-003: citas del día (zona del laboratorio) sin datos personales. */
    DailyAppointments dailyAppointments(LocalDate date);
}
