package co.academy.citas.application.port.in;

import co.academy.citas.application.port.out.AppointmentReminderPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentReminderUseCase {
    record DueReminders(String windowCode, LocalDateTime generatedAt, List<AppointmentReminderPort.DueReminder> items) {}

    record DeliveryCommand(String windowCode, String status, String channel, String providerMessageId, String errorCode) {}

    DueReminders findDue(Integer windowHours);
    AppointmentReminderPort.Delivery recordDelivery(UUID appointmentId, DeliveryCommand command);
}
