package co.academy.citas.adapter.out.persistence;

import co.academy.citas.application.port.out.AppointmentFlowPort;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppointmentFlowPersistenceAdapter implements AppointmentFlowPort {
    private final JdbcTemplate jdbc;
    public AppointmentFlowPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<UUID> professionalForUser(UUID userId) {
        return jdbc.query("select id from professional_profile where user_id = ?", (rs, row) -> uuid(rs.getBytes(1)), bytes(userId)).stream().findFirst();
    }
    @Override public boolean eligible(UUID professionalId, long locationId, long specialtyId) {
        String specialty = specialtyId < 0 ? "" : " and exists (select 1 from professional_specialty ps where ps.professional_id = p.id and ps.specialty_id = ?)";
        List<Integer> result = specialtyId < 0
                ? jdbc.query("select 1 from professional_profile p join professional_location pl on pl.professional_id = p.id where p.id = ? and p.active = true and pl.location_id = ?", (rs,row)->1, bytes(professionalId), locationId)
                : jdbc.query("select 1 from professional_profile p join professional_location pl on pl.professional_id = p.id where p.id = ? and p.active = true and pl.location_id = ?" + specialty, (rs,row)->1, bytes(professionalId), locationId, specialtyId);
        return !result.isEmpty();
    }
    @Override public Optional<SpecialtyDetails> specialty(long specialtyId) {
        return jdbc.query("select id, code, name, duration_minutes, active from specialty_catalog where id = ?", (rs,row) -> new SpecialtyDetails(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getBoolean(5)), specialtyId).stream().findFirst();
    }
    @Override public void createBlock(UUID blockId, UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) {
        Integer count = jdbc.queryForObject("select count(*) from availability_block where professional_id = ? and starts_at < ? and ends_at > ?", Integer.class, bytes(professionalId), endsAt, startsAt);
        if (count != null && count > 0) throw new IllegalArgumentException("Availability blocks cannot overlap");
        jdbc.update("insert into availability_block (id, professional_id, location_id, starts_at, ends_at) values (?, ?, ?, ?, ?)", bytes(blockId), bytes(professionalId), locationId, startsAt, endsAt);
        for (LocalDateTime slot = startsAt; slot.isBefore(endsAt); slot = slot.plusMinutes(30)) jdbc.update("insert into professional_slot (availability_block_id, professional_id, location_id, starts_at, ends_at) values (?, ?, ?, ?, ?)", bytes(blockId), bytes(professionalId), locationId, slot, slot.plusMinutes(30));
    }
    @Override public List<Slot> availableSlots(long locationId, long specialtyId, UUID professionalId, LocalDate date) {
        return jdbc.query("select starts_at, ends_at from professional_slot where professional_id = ? and location_id = ? and appointment_id is null and cast(starts_at as date) = ? order by starts_at", (rs,row)->new Slot(rs.getTimestamp(1).toLocalDateTime(),rs.getTimestamp(2).toLocalDateTime()), bytes(professionalId), locationId, date);
    }
    @Override public boolean reserve(UUID appointmentId, UUID patientId, UUID professionalId, long locationId, long specialtyId, String status, LocalDateTime startsAt, LocalDateTime endsAt, String reason) {
        List<Long> ids = jdbc.query("select id from professional_slot where professional_id = ? and location_id = ? and starts_at >= ? and ends_at <= ? and appointment_id is null order by starts_at for update", (rs,row)->rs.getLong(1), bytes(professionalId), locationId, startsAt, endsAt);
        int expected = (int) java.time.Duration.between(startsAt, endsAt).toMinutes() / 30;
        if (ids.size() != expected || !consecutive(ids, professionalId, locationId, startsAt, expected)) return false;
        jdbc.update("insert into appointment_record (id, patient_user_id, professional_id, location_id, specialty_id, status_code, scheduled_start_at, scheduled_end_at, reason) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", bytes(appointmentId), bytes(patientId), bytes(professionalId), locationId, specialtyId, status, startsAt, endsAt, reason);
        for (Long id : ids) if (jdbc.update("update professional_slot set appointment_id = ? where id = ? and appointment_id is null", bytes(appointmentId), id) != 1) return false;
        jdbc.update("insert into appointment_status_history (appointment_id, status_code, changed_by_user_id, change_source, reason) values (?, ?, ?, ?, ?)", bytes(appointmentId), status, bytes(patientId), "USER", reason);
        return true;
    }
    @Override public List<Appointment> pendingAppointments() { return appointments("where a.status_code = 'REQUESTED' order by a.scheduled_start_at", new Object[]{}); }
    @Override public Optional<Appointment> findAppointment(UUID id) { return appointments("where a.id = ?", new Object[]{bytes(id)}).stream().findFirst(); }
    @Override public boolean decide(UUID appointmentId, UUID adminId, String status, String reason) {
        int changed = jdbc.update("update appointment_record set status_code = ?, decision_reason = ?, decided_by_user_id = ?, decided_at = current_timestamp where id = ? and status_code = 'REQUESTED'", status, reason, bytes(adminId), bytes(appointmentId));
        if (changed != 1) return false;
        if ("REJECTED".equals(status)) jdbc.update("update professional_slot set appointment_id = null where appointment_id = ?", bytes(appointmentId));
        jdbc.update("insert into appointment_status_history (appointment_id, status_code, changed_by_user_id, change_source, reason) values (?, ?, ?, ?, ?)", bytes(appointmentId), status, bytes(adminId), "ADMIN", reason);
        return true;
    }
    private boolean consecutive(List<Long> ids, UUID professionalId, long locationId, LocalDateTime start, int expected) {
        if (ids.size() != expected) return false;
        List<LocalDateTime> starts = jdbc.query("select starts_at from professional_slot where professional_id = ? and location_id = ? and starts_at >= ? order by starts_at limit " + expected, (rs,row)->rs.getTimestamp(1).toLocalDateTime(), bytes(professionalId), locationId, start);
        return starts.size() == expected && starts.get(0).equals(start) && starts.get(starts.size()-1).equals(start.plusMinutes(30L * (expected - 1)));
    }
    private List<Appointment> appointments(String where, Object[] params) {
        return jdbc.query("select a.id, a.patient_user_id, a.professional_id, a.location_id, a.specialty_id, s.name, a.status_code, a.scheduled_start_at, a.scheduled_end_at, a.reason, a.decision_reason from appointment_record a join specialty_catalog s on s.id = a.specialty_id " + where, (rs,row)->appointment(rs), params);
    }
    private Appointment appointment(ResultSet rs) throws SQLException { return new Appointment(uuid(rs.getBytes(1)),uuid(rs.getBytes(2)),uuid(rs.getBytes(3)),rs.getLong(4),rs.getLong(5),rs.getString(6),rs.getString(7),rs.getTimestamp(8).toLocalDateTime(),rs.getTimestamp(9).toLocalDateTime(),rs.getString(10),rs.getString(11)); }
    private static byte[] bytes(UUID value) { return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array(); }
    private static UUID uuid(byte[] value) { ByteBuffer b=ByteBuffer.wrap(value); return new UUID(b.getLong(),b.getLong()); }
}
