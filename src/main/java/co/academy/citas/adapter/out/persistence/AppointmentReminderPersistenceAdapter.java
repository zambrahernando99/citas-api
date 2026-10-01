package co.academy.citas.adapter.out.persistence;

import co.academy.citas.application.port.out.AppointmentReminderPort;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppointmentReminderPersistenceAdapter implements AppointmentReminderPort {
    private final JdbcTemplate jdbc;
    public AppointmentReminderPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<DueReminder> findDue(LocalDateTime fromExclusive, LocalDateTime toInclusive, String windowCode, int maxAttempts, int limit) {
        return jdbc.query("select a.id, a.scheduled_start_at, a.scheduled_end_at, l.name, s.name, concat(pro.given_names, ' ', pro.family_names), patient.given_names, patient.email"
                        + " from appointment_record a join clinic_location l on l.id = a.location_id join specialty_catalog s on s.id = a.specialty_id"
                        + " join professional_profile p on p.id = a.professional_id join user_account pro on pro.id = p.user_id"
                        + " join user_account patient on patient.id = a.patient_user_id"
                        + " left join appointment_reminder_delivery d on d.appointment_id = a.id and d.window_code = ?"
                        + " where a.status_code = 'APPROVED' and a.scheduled_start_at > ? and a.scheduled_start_at <= ?"
                        + " and (d.id is null or (d.status_code = 'FAILED' and d.attempt_count < ?))"
                        + " order by a.scheduled_start_at limit ?",
                (rs, row) -> new DueReminder(uuid(rs.getBytes(1)), rs.getObject(2, LocalDateTime.class), rs.getObject(3, LocalDateTime.class),
                        rs.getString(4), rs.getString(5), rs.getString(6), firstName(rs.getString(7)), rs.getString(8)),
                windowCode, fromExclusive, toInclusive, maxAttempts, limit);
    }

    @Override public Optional<String> appointmentStatus(UUID appointmentId) {
        return jdbc.query("select status_code from appointment_record where id = ?", (rs, row) -> rs.getString(1), bytes(appointmentId)).stream().findFirst();
    }

    @Override public Optional<Delivery> findDelivery(UUID appointmentId, String windowCode) {
        return jdbc.query("select channel_code, status_code, attempt_count, provider_message_id, error_code from appointment_reminder_delivery where appointment_id = ? and window_code = ?",
                (rs, row) -> new Delivery(appointmentId, windowCode, rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5)),
                bytes(appointmentId), windowCode).stream().findFirst();
    }

    @Override public boolean insertDelivery(Delivery delivery) {
        try {
            jdbc.update("insert into appointment_reminder_delivery (appointment_id, window_code, channel_code, status_code, attempt_count, provider_message_id, error_code) values (?,?,?,?,?,?,?)",
                    bytes(delivery.appointmentId()), delivery.windowCode(), delivery.channel(), delivery.status(), delivery.attemptCount(),
                    delivery.providerMessageId(), delivery.errorCode());
            return true;
        } catch (DuplicateKeyException alreadyRecorded) {
            return false;
        }
    }

    @Override public void updateDelivery(Delivery delivery) {
        // La condición status_code = 'FAILED' evita degradar un SENT registrado por una ejecución concurrente
        jdbc.update("update appointment_reminder_delivery set status_code = ?, attempt_count = ?, provider_message_id = ?, error_code = ?, updated_at = current_timestamp"
                        + " where appointment_id = ? and window_code = ? and status_code = 'FAILED'",
                delivery.status(), delivery.attemptCount(), delivery.providerMessageId(), delivery.errorCode(),
                bytes(delivery.appointmentId()), delivery.windowCode());
    }

    private static String firstName(String givenNames) { return givenNames == null ? null : givenNames.trim().split("\\s+")[0]; }
    private static byte[] bytes(UUID value) { return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array(); }
    private static UUID uuid(byte[] value) { ByteBuffer b = ByteBuffer.wrap(value); return new UUID(b.getLong(), b.getLong()); }
}
