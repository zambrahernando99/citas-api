package co.academy.citas.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.academy.citas.application.exception.InvalidReminderRequestException;
import co.academy.citas.application.port.in.AppointmentReminderUseCase.DeliveryCommand;
import co.academy.citas.application.port.out.AppointmentReminderPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppointmentReminderServiceTest {
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    final FakePort port = new FakePort();
    final AppointmentReminderService service = new AppointmentReminderService(port, CLOCK, 24, 3);

    @Test void windowIsComputedFromTheInjectedClockInUtc() {
        var result = service.findDue(null);
        assertThat(result.windowCode()).isEqualTo("H24");
        assertThat(port.from).isEqualTo(LocalDateTime.of(2026, 10, 1, 12, 0));
        assertThat(port.to).isEqualTo(LocalDateTime.of(2026, 10, 2, 12, 0));
        assertThat(port.maxAttempts).isEqualTo(3);
        service.findDue(72);
        assertThat(port.to).isEqualTo(LocalDateTime.of(2026, 10, 4, 12, 0));
    }

    @Test void windowBoundsAreEnforced() {
        assertThatThrownBy(() -> service.findDue(0)).isInstanceOf(InvalidReminderRequestException.class);
        assertThatThrownBy(() -> service.findDue(73)).isInstanceOf(InvalidReminderRequestException.class);
    }

    @Test void windowCodeOutsideRangeIsRejectedEvenIfWellFormed() {
        UUID id = UUID.randomUUID(); port.statuses.put(id, "APPROVED");
        assertThatThrownBy(() -> service.recordDelivery(id, new DeliveryCommand("H99", "SENT", null, null, null)))
                .isInstanceOf(InvalidReminderRequestException.class);
        assertThatThrownBy(() -> service.recordDelivery(id, new DeliveryCommand("H0", "SENT", null, null, null)))
                .isInstanceOf(InvalidReminderRequestException.class);
    }

    @Test void failedThenSentKeepsCountingAttempts() {
        UUID id = UUID.randomUUID(); port.statuses.put(id, "APPROVED");
        assertThat(service.recordDelivery(id, new DeliveryCommand("H24", "FAILED", null, null, "timeout")).attemptCount()).isEqualTo(1);
        var sent = service.recordDelivery(id, new DeliveryCommand("H24", "SENT", "GMAIL", "msg-9", null));
        assertThat(sent.status()).isEqualTo("SENT");
        assertThat(sent.attemptCount()).isEqualTo(2);
        assertThat(sent.errorCode()).isNull();
    }

    static class FakePort implements AppointmentReminderPort {
        LocalDateTime from, to; int maxAttempts;
        final Map<UUID, String> statuses = new HashMap<>();
        final Map<String, Delivery> deliveries = new HashMap<>();

        @Override public List<DueReminder> findDue(LocalDateTime fromExclusive, LocalDateTime toInclusive, String windowCode, int maxAttempts, int limit) {
            this.from = fromExclusive; this.to = toInclusive; this.maxAttempts = maxAttempts; return List.of();
        }
        @Override public Optional<String> appointmentStatus(UUID appointmentId) { return Optional.ofNullable(statuses.get(appointmentId)); }
        @Override public Optional<Delivery> findDelivery(UUID appointmentId, String windowCode) { return Optional.ofNullable(deliveries.get(appointmentId + windowCode)); }
        @Override public boolean insertDelivery(Delivery delivery) { return deliveries.putIfAbsent(delivery.appointmentId() + delivery.windowCode(), delivery) == null; }
        @Override public void updateDelivery(Delivery delivery) { deliveries.put(delivery.appointmentId() + delivery.windowCode(), delivery); }
    }
}
