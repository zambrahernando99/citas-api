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
    @Override public List<AvailabilityBlock> availabilityBlocks(UUID professionalId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("select id, location_id, starts_at, ends_at from availability_block where professional_id = ?");
        List<Object> args = new ArrayList<>(); args.add(bytes(professionalId));
        if (from != null) { sql.append(" and starts_at >= ?"); args.add(from.atStartOfDay()); }
        if (to != null) { sql.append(" and starts_at < ?"); args.add(to.plusDays(1).atStartOfDay()); }
        sql.append(" order by starts_at");
        return jdbc.query(sql.toString(), (rs, row) -> new AvailabilityBlock(uuid(rs.getBytes(1)), rs.getLong(2),
                rs.getTimestamp(3).toLocalDateTime(), rs.getTimestamp(4).toLocalDateTime()), args.toArray());
    }
    @Override public boolean updateBlock(UUID blockId, UUID professionalId, long locationId, LocalDateTime startsAt, LocalDateTime endsAt) {
        List<Integer> existing = jdbc.query("select 1 from availability_block where id = ? and professional_id = ? and starts_at > current_timestamp for update",
                (rs, row) -> 1, bytes(blockId), bytes(professionalId));
        if (existing.isEmpty()) return false;
        Integer committed = jdbc.queryForObject("select count(*) from professional_slot where availability_block_id = ? and (appointment_id is not null or reschedule_request_id is not null)", Integer.class, bytes(blockId));
        Integer overlapping = jdbc.queryForObject("select count(*) from availability_block where professional_id = ? and id <> ? and starts_at < ? and ends_at > ?", Integer.class, bytes(professionalId), bytes(blockId), endsAt, startsAt);
        if ((committed != null && committed > 0) || (overlapping != null && overlapping > 0)) return false;
        jdbc.update("delete from professional_slot where availability_block_id = ?", bytes(blockId));
        int changed = jdbc.update("update availability_block set location_id = ?, starts_at = ?, ends_at = ? where id = ? and professional_id = ?",
                locationId, startsAt, endsAt, bytes(blockId), bytes(professionalId));
        if (changed != 1) return false;
        for (LocalDateTime slot = startsAt; slot.isBefore(endsAt); slot = slot.plusMinutes(30))
            jdbc.update("insert into professional_slot (availability_block_id, professional_id, location_id, starts_at, ends_at) values (?, ?, ?, ?, ?)", bytes(blockId), bytes(professionalId), locationId, slot, slot.plusMinutes(30));
        return true;
    }
    @Override public boolean deleteBlock(UUID blockId, UUID professionalId) {
        List<Integer> existing = jdbc.query("select 1 from availability_block where id = ? and professional_id = ? and starts_at > current_timestamp for update",
                (rs, row) -> 1, bytes(blockId), bytes(professionalId));
        if (existing.isEmpty()) return false;
        Integer committed = jdbc.queryForObject("select count(*) from professional_slot where availability_block_id = ? and (appointment_id is not null or reschedule_request_id is not null)", Integer.class, bytes(blockId));
        if (committed != null && committed > 0) return false;
        jdbc.update("delete from professional_slot where availability_block_id = ?", bytes(blockId));
        return jdbc.update("delete from availability_block where id = ? and professional_id = ?", bytes(blockId), bytes(professionalId)) == 1;
    }
    @Override public List<Slot> availableSlots(long locationId, long specialtyId, UUID professionalId, LocalDate date) {
        return jdbc.query("select starts_at, ends_at from professional_slot where professional_id = ? and location_id = ? and appointment_id is null and reschedule_request_id is null and cast(starts_at as date) = ? order by starts_at", (rs,row)->new Slot(rs.getTimestamp(1).toLocalDateTime(),rs.getTimestamp(2).toLocalDateTime()), bytes(professionalId), locationId, date);
    }
    @Override public boolean reserve(UUID appointmentId, UUID patientId, UUID professionalId, long locationId, long specialtyId, String status, LocalDateTime startsAt, LocalDateTime endsAt, String reason) {
        List<Long> ids = jdbc.query("select id from professional_slot where professional_id = ? and location_id = ? and starts_at >= ? and ends_at <= ? and appointment_id is null and reschedule_request_id is null order by starts_at for update", (rs,row)->rs.getLong(1), bytes(professionalId), locationId, startsAt, endsAt);
        int expected = (int) java.time.Duration.between(startsAt, endsAt).toMinutes() / 30;
        if (ids.size() != expected || !consecutive(ids, professionalId, locationId, startsAt, expected)) return false;
        jdbc.update("insert into appointment_record (id, patient_user_id, professional_id, location_id, specialty_id, status_code, scheduled_start_at, scheduled_end_at, reason) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", bytes(appointmentId), bytes(patientId), bytes(professionalId), locationId, specialtyId, status, startsAt, endsAt, reason);
        for (Long id : ids) if (jdbc.update("update professional_slot set appointment_id = ? where id = ? and appointment_id is null", bytes(appointmentId), id) != 1) return false;
        jdbc.update("insert into appointment_status_history (appointment_id, status_code, changed_by_user_id, change_source, reason) values (?, ?, ?, ?, ?)", bytes(appointmentId), status, bytes(patientId), "USER", reason);
        return true;
    }
    @Override public List<Appointment> pendingAppointments() { return appointments("where a.status_code = 'REQUESTED' order by a.scheduled_start_at", new Object[]{}); }
    @Override public List<Appointment> userAppointments(UUID patientId, String status, LocalDate from, LocalDate to) {
        StringBuilder where = new StringBuilder("where a.patient_user_id = ?");
        List<Object> args = new ArrayList<>(); args.add(bytes(patientId));
        if (status != null && !status.isBlank()) { where.append(" and a.status_code = ?"); args.add(status); }
        if (from != null) { where.append(" and a.scheduled_start_at >= ?"); args.add(from.atStartOfDay()); }
        if (to != null) { where.append(" and a.scheduled_start_at < ?"); args.add(to.plusDays(1).atStartOfDay()); }
        where.append(" order by a.scheduled_start_at desc");
        return appointments(where.toString(), args.toArray());
    }
    @Override public List<RescheduleRequest> pendingReschedules() {
        return jdbc.query(rescheduleSelect() + " where r.status_code = 'PENDING' order by r.created_at", (rs, row) -> reschedule(rs));
    }
    @Override public List<Appointment> professionalAppointments(UUID professionalId, LocalDate from, LocalDate to, Long locationId) {
        StringBuilder where = new StringBuilder("where a.professional_id = ? and a.status_code = 'APPROVED'");
        List<Object> args = new ArrayList<>(); args.add(bytes(professionalId));
        if (from != null) { where.append(" and a.scheduled_start_at >= ?"); args.add(from.atStartOfDay()); }
        if (to != null) { where.append(" and a.scheduled_start_at < ?"); args.add(to.plusDays(1).atStartOfDay()); }
        if (locationId != null) { where.append(" and a.location_id = ?"); args.add(locationId); }
        where.append(" order by a.scheduled_start_at");
        return appointments(where.toString(), args.toArray());
    }
    @Override public Optional<Appointment> findAppointment(UUID id) { return appointments("where a.id = ?", new Object[]{bytes(id)}).stream().findFirst(); }
    @Override public boolean decide(UUID appointmentId, UUID adminId, String status, String reason) {
        int changed = jdbc.update("update appointment_record set status_code = ?, decision_reason = ?, decided_by_user_id = ?, decided_at = current_timestamp where id = ? and status_code = 'REQUESTED'", status, reason, bytes(adminId), bytes(appointmentId));
        if (changed != 1) return false;
        if ("REJECTED".equals(status)) jdbc.update("update professional_slot set appointment_id = null where appointment_id = ?", bytes(appointmentId));
        jdbc.update("insert into appointment_status_history (appointment_id, status_code, changed_by_user_id, change_source, reason) values (?, ?, ?, ?, ?)", bytes(appointmentId), status, bytes(adminId), "ADMIN", reason);
        return true;
    }
    @Override public boolean cancel(UUID appointmentId, UUID patientId, String reason, LocalDateTime now) {
        int changed = jdbc.update("update appointment_record set status_code = 'CANCELLED' where id = ? and patient_user_id = ? and status_code in ('REQUESTED','APPROVED') and scheduled_start_at > ? and not exists (select 1 from appointment_reschedule r where r.appointment_id = appointment_record.id and r.status_code = 'PENDING')",
                bytes(appointmentId), bytes(patientId), now);
        if (changed != 1) return false;
        jdbc.update("update professional_slot set appointment_id = null where appointment_id = ?", bytes(appointmentId));
        jdbc.update("insert into appointment_status_history (appointment_id, status_code, changed_by_user_id, change_source, reason) values (?, 'CANCELLED', ?, 'USER', ?)",
                bytes(appointmentId), bytes(patientId), reason);
        return true;
    }
    @Override public boolean closeAppointment(UUID appointmentId, UUID professionalId, String status, LocalDateTime now) {
        List<byte[]> userIds = jdbc.query("select user_id from professional_profile where id = ?", (rs, row) -> rs.getBytes(1), bytes(professionalId));
        if (userIds.isEmpty()) return false;
        int changed = jdbc.update("update appointment_record set status_code = ? where id = ? and professional_id = ? and status_code = 'APPROVED' and scheduled_end_at <= ? and not exists (select 1 from appointment_reschedule r where r.appointment_id = appointment_record.id and r.status_code = 'PENDING')",
                status, bytes(appointmentId), bytes(professionalId), now);
        if (changed != 1) return false;
        jdbc.update("insert into appointment_status_history (appointment_id, status_code, changed_by_user_id, change_source) values (?, ?, ?, 'USER')",
                bytes(appointmentId), status, userIds.get(0));
        return true;
    }
    @Override public Optional<RescheduleRequest> requestReschedule(UUID requestId, UUID appointmentId, UUID patientId,
                                                                    LocalDateTime startsAt, LocalDateTime endsAt, String reason) {
        List<byte[]> locked = jdbc.query("select id from appointment_record where id = ? and patient_user_id = ? and status_code = 'APPROVED' for update",
                (rs, row) -> rs.getBytes(1), bytes(appointmentId), bytes(patientId));
        if (locked.isEmpty()) return Optional.empty();
        List<Appointment> originals = appointments("where a.id = ? and a.patient_user_id = ? and a.status_code = 'APPROVED'",
                new Object[]{bytes(appointmentId), bytes(patientId)});
        if (originals.isEmpty() || !jdbc.query("select id from appointment_reschedule where appointment_id = ? and status_code = 'PENDING'",
                (rs, row) -> rs.getLong(1), bytes(appointmentId)).isEmpty()) return Optional.empty();
        Appointment original = originals.get(0);
        List<Long> slotIds = jdbc.query("select id from professional_slot where professional_id = ? and location_id = ? and starts_at >= ? and ends_at <= ? and appointment_id is null and reschedule_request_id is null order by starts_at for update",
                (rs, row) -> rs.getLong(1), bytes(original.professionalId()), original.locationId(), startsAt, endsAt);
        int expected = original.durationMinutes() / 30;
        if (!consecutive(slotIds, original.professionalId(), original.locationId(), startsAt, expected)) return Optional.empty();
        jdbc.update("insert into appointment_reschedule (id, appointment_id, requested_by_user_id, status_code, proposed_start_at, proposed_end_at, reason) values (?, ?, ?, 'PENDING', ?, ?, ?)",
                bytes(requestId), bytes(appointmentId), bytes(patientId), startsAt, endsAt, reason);
        for (Long slotId : slotIds) if (jdbc.update("update professional_slot set reschedule_request_id = ? where id = ? and appointment_id is null and reschedule_request_id is null",
                bytes(requestId), slotId) != 1) return Optional.empty();
        return findReschedule(requestId);
    }
    @Override public Optional<RescheduleRequest> decideReschedule(UUID requestId, UUID adminId, boolean approve, String reason) {
        List<byte[]> appointmentIds = jdbc.query("select appointment_id from appointment_reschedule where id = ? and status_code = 'PENDING'",
                (rs, row) -> rs.getBytes(1), bytes(requestId));
        if (appointmentIds.isEmpty()) return Optional.empty();
        List<String> appointmentStates = jdbc.query("select status_code from appointment_record where id = ? for update",
                (rs, row) -> rs.getString(1), appointmentIds.get(0));
        if (appointmentStates.isEmpty() || !"APPROVED".equals(appointmentStates.get(0))) return Optional.empty();
        List<RescheduleRequest> pending = jdbc.query(rescheduleSelect() + " where r.id = ? and r.status_code = 'PENDING' for update",
                (rs, row) -> reschedule(rs), bytes(requestId));
        if (pending.isEmpty()) return Optional.empty();
        RescheduleRequest request = pending.get(0);
        int changed = jdbc.update("update appointment_reschedule set status_code = ?, decision_reason = ?, decided_by_user_id = ?, decided_at = current_timestamp where id = ? and status_code = 'PENDING'",
                approve ? "APPROVED" : "REJECTED", reason, bytes(adminId), bytes(requestId));
        if (changed != 1) return Optional.empty();
        if (approve) {
            jdbc.update("update professional_slot set appointment_id = null where appointment_id = ?", bytes(request.appointmentId()));
            jdbc.update("update appointment_record set scheduled_start_at = ?, scheduled_end_at = ? where id = ?",
                    request.startsAt(), request.endsAt(), bytes(request.appointmentId()));
            jdbc.update("update professional_slot set appointment_id = ?, reschedule_request_id = null where reschedule_request_id = ?",
                    bytes(request.appointmentId()), bytes(requestId));
        } else {
            jdbc.update("update professional_slot set reschedule_request_id = null where reschedule_request_id = ?", bytes(requestId));
        }
        return findReschedule(requestId);
    }
    private Optional<RescheduleRequest> findReschedule(UUID id) {
        return reschedules("where r.id = ?", bytes(id)).stream().findFirst();
    }
    private List<RescheduleRequest> reschedules(String where, byte[] id) {
        return jdbc.query(rescheduleSelect() + " " + where, (rs, row) -> reschedule(rs), id);
    }
    private String rescheduleSelect() { return "select r.id, r.appointment_id, r.status_code, r.proposed_start_at, r.proposed_end_at, r.reason, r.decision_reason, concat(patient.given_names, ' ', patient.family_names), s.name, concat(pro.given_names, ' ', pro.family_names), l.name, a.scheduled_start_at from appointment_reschedule r join appointment_record a on a.id = r.appointment_id join specialty_catalog s on s.id = a.specialty_id join professional_profile p on p.id = a.professional_id join user_account pro on pro.id = p.user_id join user_account patient on patient.id = a.patient_user_id join clinic_location l on l.id = a.location_id"; }
    private RescheduleRequest reschedule(ResultSet rs) throws SQLException {
        return new RescheduleRequest(uuid(rs.getBytes(1)), uuid(rs.getBytes(2)), rs.getString(3),
                rs.getTimestamp(4).toLocalDateTime(), rs.getTimestamp(5).toLocalDateTime(), rs.getString(6),
                rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11),
                rs.getTimestamp(12).toLocalDateTime());
    }
    private boolean consecutive(List<Long> ids, UUID professionalId, long locationId, LocalDateTime start, int expected) {
        if (ids.size() != expected) return false;
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        List<Object> arguments = new ArrayList<>();
        arguments.add(bytes(professionalId)); arguments.add(locationId); arguments.addAll(ids);
        List<Slot> slots = jdbc.query("select starts_at, ends_at from professional_slot where professional_id = ? and location_id = ? and id in (" + placeholders + ") order by starts_at",
                (rs,row)->new Slot(rs.getTimestamp(1).toLocalDateTime(),rs.getTimestamp(2).toLocalDateTime()), arguments.toArray());
        if (slots.size() != expected || !slots.get(0).startsAt().equals(start)) return false;
        for (int index = 0; index < slots.size(); index++) {
            Slot slot = slots.get(index);
            if (!slot.endsAt().equals(slot.startsAt().plusMinutes(30))) return false;
            if (index > 0 && !slots.get(index - 1).endsAt().equals(slot.startsAt())) return false;
        }
        return true;
    }
    private List<Appointment> appointments(String where, Object[] params) {
        return jdbc.query("select a.id, a.patient_user_id, a.professional_id, a.location_id, a.specialty_id, s.name, a.status_code, a.scheduled_start_at, a.scheduled_end_at, a.reason, a.decision_reason, concat(pro.given_names, ' ', pro.family_names), l.name, concat(patient.given_names, ' ', patient.family_names), exists(select 1 from appointment_reschedule ar where ar.appointment_id = a.id and ar.status_code = 'PENDING') from appointment_record a join specialty_catalog s on s.id = a.specialty_id join professional_profile p on p.id = a.professional_id join user_account pro on pro.id = p.user_id join user_account patient on patient.id = a.patient_user_id join clinic_location l on l.id = a.location_id " + where, (rs,row)->appointment(rs), params);
    }
    private Appointment appointment(ResultSet rs) throws SQLException { return new Appointment(uuid(rs.getBytes(1)),uuid(rs.getBytes(2)),uuid(rs.getBytes(3)),rs.getLong(4),rs.getLong(5),rs.getString(6),rs.getString(7),rs.getTimestamp(8).toLocalDateTime(),rs.getTimestamp(9).toLocalDateTime(),rs.getString(10),rs.getString(11),rs.getString(12),rs.getString(13),rs.getString(14),rs.getBoolean(15)); }
    private static byte[] bytes(UUID value) { return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array(); }
    private static UUID uuid(byte[] value) { ByteBuffer b=ByteBuffer.wrap(value); return new UUID(b.getLong(),b.getLong()); }
}
