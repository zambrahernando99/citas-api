package co.academy.citas.application.service;

import co.academy.citas.application.exception.AppointmentNotFoundException;
import co.academy.citas.application.exception.InvalidReminderRequestException;
import co.academy.citas.application.exception.ReminderNotApplicableException;
import co.academy.citas.application.port.in.AppointmentReminderUseCase;
import co.academy.citas.application.port.out.AppointmentReminderPort;
import co.academy.citas.application.port.out.AppointmentReminderPort.Delivery;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * WF-001 (S5): selecciona citas APPROVED dentro de una ventana y registra el resultado de entrega.
 * La idempotencia es por (cita, ventana): SENT es definitivo y FAILED se reintenta hasta maxAttempts (DEC-014).
 */
public class AppointmentReminderService implements AppointmentReminderUseCase {
    static final int MIN_WINDOW_HOURS = 1, MAX_WINDOW_HOURS = 72, BATCH_LIMIT = 200;
    private static final Pattern WINDOW_CODE = Pattern.compile("H([1-9]|[1-6][0-9]|7[0-2])");
    private static final Set<String> STATUSES = Set.of("SENT", "FAILED");
    private static final String CHANNEL = "GMAIL";

    private final AppointmentReminderPort port;
    private final Clock clock;
    private final int defaultWindowHours;
    private final int maxAttempts;

    public AppointmentReminderService(AppointmentReminderPort port, Clock clock, int defaultWindowHours, int maxAttempts) {
        if (defaultWindowHours < MIN_WINDOW_HOURS || defaultWindowHours > MAX_WINDOW_HOURS || maxAttempts < 1)
            throw new IllegalArgumentException("Invalid reminder configuration");
        this.port = port; this.clock = clock; this.defaultWindowHours = defaultWindowHours; this.maxAttempts = maxAttempts;
    }

    @Override
    public DueReminders findDue(Integer windowHours) {
        int hours = windowHours == null ? defaultWindowHours : windowHours;
        if (hours < MIN_WINDOW_HOURS || hours > MAX_WINDOW_HOURS)
            throw new InvalidReminderRequestException("windowHours must be between 1 and 72");
        LocalDateTime now = LocalDateTime.now(clock);
        String windowCode = "H" + hours;
        return new DueReminders(windowCode, now, port.findDue(now, now.plusHours(hours), windowCode, maxAttempts, BATCH_LIMIT));
    }

    @Override
    public Delivery recordDelivery(UUID appointmentId, DeliveryCommand command) {
        if (command.windowCode() == null || !WINDOW_CODE.matcher(command.windowCode()).matches())
            throw new InvalidReminderRequestException("windowCode is invalid");
        if (command.status() == null || !STATUSES.contains(command.status()))
            throw new InvalidReminderRequestException("status must be SENT or FAILED");
        if (command.channel() != null && !CHANNEL.equals(command.channel()))
            throw new InvalidReminderRequestException("channel must be GMAIL");
        String status = port.appointmentStatus(appointmentId).orElseThrow(AppointmentNotFoundException::new);
        if (!"APPROVED".equals(status)) throw new ReminderNotApplicableException();

        boolean sent = "SENT".equals(command.status());
        Delivery first = new Delivery(appointmentId, command.windowCode(), CHANNEL, command.status(), 1,
                sent ? command.providerMessageId() : null, sent ? null : command.errorCode());
        if (port.insertDelivery(first)) return first;

        Delivery existing = port.findDelivery(appointmentId, command.windowCode()).orElseThrow(AppointmentNotFoundException::new);
        // SENT es definitivo: repetir SENT o recibir un FAILED tardío no cambia nada
        if ("SENT".equals(existing.status())) return existing;
        Delivery next = new Delivery(appointmentId, command.windowCode(), CHANNEL, command.status(), existing.attemptCount() + 1,
                sent ? command.providerMessageId() : null, sent ? null : command.errorCode());
        port.updateDelivery(next);
        return next;
    }
}
