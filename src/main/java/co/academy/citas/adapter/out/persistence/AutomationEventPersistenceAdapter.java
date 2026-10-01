package co.academy.citas.adapter.out.persistence;

import co.academy.citas.application.port.out.AutomationEventPort;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AutomationEventPersistenceAdapter implements AutomationEventPort {
    private final JdbcTemplate jdbc;
    public AutomationEventPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void enqueue(UUID eventId, String eventType, String newStatus, UUID appointmentId, UUID rescheduleId, LocalDateTime occurredAt) {
        jdbc.update("insert into automation_event_outbox (id, event_type, new_status, appointment_id, reschedule_id, occurred_at, delivery_status, attempt_count, next_attempt_at) values (?,?,?,?,?,?,'PENDING',0,?)",
                bytes(eventId), eventType, newStatus, bytes(appointmentId), rescheduleId == null ? null : bytes(rescheduleId), occurredAt, occurredAt);
    }

    @Override public List<PendingEvent> findDispatchable(LocalDateTime now, int limit) {
        return jdbc.query("select e.id, e.event_type, e.new_status, e.occurred_at, e.attempt_count, a.id, a.scheduled_start_at, a.scheduled_end_at, l.name, s.name,"
                        + " concat(pro.given_names, ' ', pro.family_names), patient.given_names, patient.email,"
                        + " case when e.reschedule_id is not null then r.decision_reason else a.decision_reason end"
                        + " from automation_event_outbox e join appointment_record a on a.id = e.appointment_id"
                        + " left join appointment_reschedule r on r.id = e.reschedule_id"
                        + " join clinic_location l on l.id = a.location_id join specialty_catalog s on s.id = a.specialty_id"
                        + " join professional_profile p on p.id = a.professional_id join user_account pro on pro.id = p.user_id"
                        + " join user_account patient on patient.id = a.patient_user_id"
                        + " where e.delivery_status = 'PENDING' and e.next_attempt_at <= ? order by e.occurred_at limit ?",
                (rs, row) -> new PendingEvent(uuid(rs.getBytes(1)), rs.getString(2), rs.getString(3), rs.getObject(4, LocalDateTime.class), rs.getInt(5),
                        uuid(rs.getBytes(6)), rs.getObject(7, LocalDateTime.class), rs.getObject(8, LocalDateTime.class), rs.getString(9), rs.getString(10),
                        rs.getString(11), firstName(rs.getString(12)), rs.getString(13), rs.getString(14)),
                now, limit);
    }

    @Override public void markDelivered(UUID eventId, int attemptCount, int httpStatus, LocalDateTime deliveredAt) {
        jdbc.update("update automation_event_outbox set delivery_status = 'DELIVERED', attempt_count = ?, last_http_status = ?, last_error = null, delivered_at = ? where id = ?",
                attemptCount, httpStatus, deliveredAt, bytes(eventId));
    }

    @Override public void markRetry(UUID eventId, int attemptCount, Integer httpStatus, String error, LocalDateTime nextAttemptAt) {
        jdbc.update("update automation_event_outbox set attempt_count = ?, last_http_status = ?, last_error = ?, next_attempt_at = ? where id = ?",
                attemptCount, httpStatus, error, nextAttemptAt, bytes(eventId));
    }

    @Override public void markFailed(UUID eventId, int attemptCount, Integer httpStatus, String error) {
        jdbc.update("update automation_event_outbox set delivery_status = 'FAILED', attempt_count = ?, last_http_status = ?, last_error = ? where id = ?",
                attemptCount, httpStatus, error, bytes(eventId));
    }

    @Override public List<DailyAppointment> appointmentsBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbc.query("select a.id, a.scheduled_start_at, a.status_code, l.name, s.name from appointment_record a"
                        + " join clinic_location l on l.id = a.location_id join specialty_catalog s on s.id = a.specialty_id"
                        + " where a.scheduled_start_at >= ? and a.scheduled_start_at < ? order by a.scheduled_start_at",
                (rs, row) -> new DailyAppointment(uuid(rs.getBytes(1)), rs.getObject(2, LocalDateTime.class), rs.getString(3), rs.getString(4), rs.getString(5)),
                fromInclusive, toExclusive);
    }

    private static String firstName(String givenNames) { return givenNames == null ? null : givenNames.trim().split("\\s+")[0]; }
    private static byte[] bytes(UUID value) { return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array(); }
    private static UUID uuid(byte[] value) { ByteBuffer b = ByteBuffer.wrap(value); return new UUID(b.getLong(), b.getLong()); }
}
