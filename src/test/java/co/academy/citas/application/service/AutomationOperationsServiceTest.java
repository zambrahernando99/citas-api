package co.academy.citas.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.academy.citas.application.port.out.AutomationEventPort;
import co.academy.citas.application.port.out.StatusWebhookPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AutomationOperationsServiceTest {
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 1, 12, 0);
    final FakeEvents events = new FakeEvents();
    final FakeWebhook webhook = new FakeWebhook();
    final AutomationOperationsService service = new AutomationOperationsService(events, webhook, CLOCK, ZoneId.of("America/Bogota"), 3);

    @Test void disabledWebhookLeavesEventsPending() {
        webhook.enabled = false; events.pending.add(event(0, "APPROVED", null));
        assertThat(service.dispatchPendingEvents().delivered()).isZero();
        assertThat(events.marks).isEmpty();
    }

    @Test void successfulDeliveryIsMarked() {
        webhook.status = 200; events.pending.add(event(0, "APPROVED", null));
        assertThat(service.dispatchPendingEvents().delivered()).isEqualTo(1);
        assertThat(events.marks).containsExactly("DELIVERED:1:200");
    }

    @Test void payloadRejectedByWebhookFailsWithoutRetry() {
        webhook.status = 400; events.pending.add(event(0, "APPROVED", null));
        assertThat(service.dispatchPendingEvents().failed()).isEqualTo(1);
        assertThat(events.marks).containsExactly("FAILED:1:400:rejected_by_webhook");
    }

    @Test void inactiveWorkflowOrAuthErrorsAreRetriedNotDropped() {
        webhook.status = 404; events.pending.add(event(0, "APPROVED", null));
        service.dispatchPendingEvents();
        webhook.status = 403; events.pending.add(event(0, "APPROVED", null));
        service.dispatchPendingEvents();
        assertThat(events.marks).containsExactly("RETRY:1:404:http_404:" + NOW.plusMinutes(1), "RETRY:1:403:http_403:" + NOW.plusMinutes(1));
    }

    @Test void serverErrorsAndTimeoutsAreRetriedWithGrowingBackoff() {
        webhook.status = 503; events.pending.add(event(0, "APPROVED", null));
        service.dispatchPendingEvents();
        webhook.status = -1; events.pending.add(event(1, "APPROVED", null));
        service.dispatchPendingEvents();
        assertThat(events.marks).containsExactly("RETRY:1:503:http_503:" + NOW.plusMinutes(1), "RETRY:2:null:no_response:" + NOW.plusMinutes(5));
    }

    @Test void lastAttemptMarksTheEventFailed() {
        webhook.status = 502; events.pending.add(event(2, "APPROVED", null));
        assertThat(service.dispatchPendingEvents().failed()).isEqualTo(1);
        assertThat(events.marks).containsExactly("FAILED:3:502:http_502");
    }

    @Test void payloadCarriesOnlyTheContractFieldsAndReasonOnlyForRejections() {
        Map<String, Object> approved = AutomationOperationsService.payload(event(0, "APPROVED", "texto"));
        assertThat(approved).containsOnlyKeys("schemaVersion", "eventId", "eventType", "status", "occurredAt", "appointment", "patient", "decisionReason");
        assertThat(approved.get("decisionReason")).isNull();
        @SuppressWarnings("unchecked") Map<String, Object> patient = (Map<String, Object>) approved.get("patient");
        assertThat(patient).containsOnlyKeys("firstName", "email");
        Map<String, Object> rejected = AutomationOperationsService.payload(event(0, "REJECTED", "  " + "x".repeat(400)));
        assertThat((String) rejected.get("decisionReason")).hasSize(300);
    }

    @Test void dailyWindowUsesTheConfiguredZone() {
        var day = service.dailyAppointments(LocalDate.of(2026, 10, 1));
        assertThat(day.timezone()).isEqualTo("America/Bogota");
        assertThat(events.from).isEqualTo(LocalDateTime.of(2026, 10, 1, 5, 0));
        assertThat(events.to).isEqualTo(LocalDateTime.of(2026, 10, 2, 5, 0));
        // Sin fecha: "hoy" en Bogotá (12:00 UTC = 07:00 Bogotá del mismo día)
        assertThat(service.dailyAppointments(null).date()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    private static AutomationEventPort.PendingEvent event(int attempts, String status, String reason) {
        return new AutomationEventPort.PendingEvent(UUID.randomUUID(), "SPECIALIZED_DECISION", status, NOW.minusMinutes(1), attempts,
                UUID.randomUUID(), NOW.plusDays(1), NOW.plusDays(1).plusMinutes(30), "Sede", "Cardiología", "Pro Synthetic", "Ana", "ana@example.test", reason);
    }

    static class FakeWebhook implements StatusWebhookPort {
        boolean enabled = true; int status = 200;
        @Override public boolean enabled() { return enabled; }
        @Override public int send(String eventId, Map<String, Object> payload) { return status; }
    }

    static class FakeEvents implements AutomationEventPort {
        final List<PendingEvent> pending = new ArrayList<>();
        final List<String> marks = new ArrayList<>();
        LocalDateTime from, to;
        @Override public void enqueue(UUID eventId, String eventType, String newStatus, UUID appointmentId, UUID rescheduleId, LocalDateTime occurredAt) {}
        @Override public List<PendingEvent> findDispatchable(LocalDateTime now, int limit) { var copy = List.copyOf(pending); pending.clear(); return copy; }
        @Override public void markDelivered(UUID eventId, int attemptCount, int httpStatus, LocalDateTime deliveredAt) { marks.add("DELIVERED:" + attemptCount + ":" + httpStatus); }
        @Override public void markRetry(UUID eventId, int attemptCount, Integer httpStatus, String error, LocalDateTime nextAttemptAt) { marks.add("RETRY:" + attemptCount + ":" + httpStatus + ":" + error + ":" + nextAttemptAt); }
        @Override public void markFailed(UUID eventId, int attemptCount, Integer httpStatus, String error) { marks.add("FAILED:" + attemptCount + ":" + httpStatus + ":" + error); }
        @Override public List<DailyAppointment> appointmentsBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) { from = fromInclusive; to = toExclusive; return List.of(); }
    }
}
