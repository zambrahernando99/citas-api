package co.academy.citas.application.service;

import co.academy.citas.application.port.in.AutomationOperationsUseCase;
import co.academy.citas.application.port.out.AutomationEventPort;
import co.academy.citas.application.port.out.AutomationEventPort.PendingEvent;
import co.academy.citas.application.port.out.StatusWebhookPort;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WF-002 (S6): despacha el outbox al webhook de n8n con reintentos (DEC-018).
 * 2xx = entregado; 4xx (salvo 408/429) = rechazo definitivo del payload; 5xx, 408, 429 o sin respuesta = reintento con espera creciente.
 * WF-003: citas de un día en la zona del laboratorio, solo sede, especialidad, estado y hora (DEC-019).
 */
public class AutomationOperationsService implements AutomationOperationsUseCase {
    static final int BATCH_LIMIT = 20, MAX_REASON = 300;
    private static final Duration[] BACKOFF = {Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(15), Duration.ofMinutes(60)};

    private final AutomationEventPort events;
    private final StatusWebhookPort webhook;
    private final Clock clock;
    private final ZoneId zone;
    private final int maxAttempts;

    public AutomationOperationsService(AutomationEventPort events, StatusWebhookPort webhook, Clock clock, ZoneId zone, int maxAttempts) {
        if (maxAttempts < 1) throw new IllegalArgumentException("Invalid event configuration");
        this.events = events; this.webhook = webhook; this.clock = clock; this.zone = zone; this.maxAttempts = maxAttempts;
    }

    @Override
    public DispatchResult dispatchPendingEvents() {
        if (!webhook.enabled()) return new DispatchResult(0, 0, 0);
        int delivered = 0, retried = 0, failed = 0;
        for (PendingEvent event : events.findDispatchable(LocalDateTime.now(clock), BATCH_LIMIT)) {
            int attempt = event.attemptCount() + 1;
            int status = webhook.send(event.eventId().toString(), payload(event));
            if (status >= 200 && status < 300) {
                events.markDelivered(event.eventId(), attempt, status, LocalDateTime.now(clock)); delivered++;
            } else if (status >= 400 && status < 500 && status != 408 && status != 429) {
                events.markFailed(event.eventId(), attempt, status, "rejected_by_webhook"); failed++;
            } else if (attempt >= maxAttempts) {
                events.markFailed(event.eventId(), attempt, status < 0 ? null : status, status < 0 ? "no_response" : "http_" + status); failed++;
            } else {
                Duration wait = BACKOFF[Math.min(attempt - 1, BACKOFF.length - 1)];
                events.markRetry(event.eventId(), attempt, status < 0 ? null : status, status < 0 ? "no_response" : "http_" + status,
                        LocalDateTime.now(clock).plus(wait)); retried++;
            }
        }
        return new DispatchResult(delivered, retried, failed);
    }

    @Override
    public DailyAppointments dailyAppointments(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now(clock.withZone(zone)) : date;
        LocalDateTime from = day.atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime to = day.plusDays(1).atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        return new DailyAppointments(day, zone.getId(), LocalDateTime.now(clock), events.appointmentsBetween(from, to));
    }

    /** Contrato del webhook WF-002 (versión 1). Solo datos necesarios para el correo (DEC-016). */
    static Map<String, Object> payload(PendingEvent event) {
        Map<String, Object> appointment = new LinkedHashMap<>();
        appointment.put("id", event.appointmentId().toString());
        appointment.put("startsAt", event.startsAt().toString());
        appointment.put("endsAt", event.endsAt().toString());
        appointment.put("locationName", event.locationName());
        appointment.put("specialtyName", event.specialtyName());
        appointment.put("professionalName", event.professionalName());
        Map<String, Object> patient = new LinkedHashMap<>();
        patient.put("firstName", event.patientFirstName());
        patient.put("email", event.patientEmail());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("schemaVersion", 1);
        body.put("eventId", event.eventId().toString());
        body.put("eventType", event.eventType());
        body.put("status", event.newStatus());
        body.put("occurredAt", event.occurredAt().toString());
        body.put("appointment", appointment);
        body.put("patient", patient);
        String reason = event.decisionReason();
        body.put("decisionReason", "REJECTED".equals(event.newStatus()) && reason != null && !reason.isBlank()
                ? reason.strip().substring(0, Math.min(reason.strip().length(), MAX_REASON)) : null);
        return body;
    }
}
