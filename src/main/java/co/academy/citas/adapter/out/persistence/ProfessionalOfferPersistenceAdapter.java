package co.academy.citas.adapter.out.persistence;

import co.academy.citas.application.port.out.ProfessionalOfferPort;
import co.academy.citas.domain.professional.ClinicLocation;
import co.academy.citas.domain.professional.ProfessionalProfile;
import co.academy.citas.domain.professional.Specialty;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalOfferPersistenceAdapter implements ProfessionalOfferPort {
    private final JdbcTemplate jdbcTemplate;

    public ProfessionalOfferPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByProfessionalCode(String professionalCode) {
        return exists("select count(*) from professional_profile where professional_code = ?", professionalCode);
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return exists("select count(*) from professional_profile where license_number = ?", licenseNumber);
    }

    @Override
    public ProfessionalProfile save(ProfessionalProfile profile) {
        jdbcTemplate.update("insert into professional_profile (id, user_id, professional_code, license_number, active) values (?, ?, ?, ?, ?)",
                uuidBytes(profile.id()), uuidBytes(profile.userId()), profile.professionalCode(), profile.licenseNumber(), profile.active());
        return findById(profile.id()).orElseThrow();
    }

    @Override
    public Optional<ProfessionalProfile> findById(UUID professionalId) {
        List<ProfessionalProfile> profiles = jdbcTemplate.query("""
                select p.id, p.user_id, p.professional_code, p.license_number, p.active,
                       u.given_names, u.family_names, u.email
                from professional_profile p join user_account u on u.id = p.user_id
                where p.id = ?
                """, (resultSet, rowNumber) -> profile(resultSet), uuidBytes(professionalId));
        return profiles.stream().findFirst();
    }

    @Override
    public List<ProfessionalProfile> findAll() {
        return jdbcTemplate.query("""
                select p.id, p.user_id, p.professional_code, p.license_number, p.active,
                       u.given_names, u.family_names, u.email
                from professional_profile p join user_account u on u.id = p.user_id
                order by u.family_names, u.given_names
                """, (resultSet, rowNumber) -> profile(resultSet));
    }

    @Override
    public List<Specialty> findActiveSpecialtiesByIds(Collection<Long> ids) {
        return specialties("select id, code, name, duration_minutes, active from specialty_catalog where active = true", ids);
    }

    @Override
    public List<Specialty> findActiveSpecialties() {
        return jdbcTemplate.query("select id, code, name, duration_minutes, active from specialty_catalog where active = true order by name",
                (resultSet, rowNumber) -> specialty(resultSet));
    }

    @Override
    public List<ClinicLocation> findLocationsByIds(Collection<Long> ids) {
        return locations("select id, code, name, address, active from clinic_location where active = true", ids);
    }

    @Override
    public List<ClinicLocation> findLocations() {
        return jdbcTemplate.query("select id, code, name, address, active from clinic_location where active = true order by code",
                (resultSet, rowNumber) -> location(resultSet));
    }

    @Override
    public void replaceAssignments(UUID professionalId, Collection<Long> specialtyIds, long primarySpecialtyId,
                                   Collection<Long> locationIds) {
        jdbcTemplate.update("delete from professional_specialty where professional_id = ?", uuidBytes(professionalId));
        jdbcTemplate.update("delete from professional_location where professional_id = ?", uuidBytes(professionalId));
        specialtyIds.forEach(specialtyId -> jdbcTemplate.update("""
                insert into professional_specialty (professional_id, specialty_id, is_primary) values (?, ?, ?)
                """, uuidBytes(professionalId), specialtyId, specialtyId == primarySpecialtyId));
        locationIds.forEach(locationId -> jdbcTemplate.update("""
                insert into professional_location (professional_id, location_id) values (?, ?)
                """, uuidBytes(professionalId), locationId));
    }

    @Override
    public void updateActive(UUID professionalId, boolean active) {
        jdbcTemplate.update("update professional_profile set active = ?, updated_at = current_timestamp where id = ?", active,
                uuidBytes(professionalId));
    }

    private boolean exists(String sql, String value) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, value);
        return count != null && count > 0;
    }

    private ProfessionalProfile profile(ResultSet resultSet) throws SQLException {
        UUID id = uuid(resultSet.getBytes("id"));
        return new ProfessionalProfile(id, uuid(resultSet.getBytes("user_id")), resultSet.getString("given_names"),
                resultSet.getString("family_names"), resultSet.getString("email"), resultSet.getString("professional_code"),
                resultSet.getString("license_number"), resultSet.getBoolean("active"), specialtiesFor(id), locationsFor(id));
    }

    private List<ProfessionalProfile.SpecialtyAssignment> specialtiesFor(UUID professionalId) {
        return jdbcTemplate.query("""
                select s.id, s.code, s.name, s.duration_minutes, s.active, ps.is_primary
                from professional_specialty ps join specialty_catalog s on s.id = ps.specialty_id
                where ps.professional_id = ? order by ps.is_primary desc, s.name
                """, (resultSet, rowNumber) -> new ProfessionalProfile.SpecialtyAssignment(specialty(resultSet),
                resultSet.getBoolean("is_primary")), uuidBytes(professionalId));
    }

    private List<ClinicLocation> locationsFor(UUID professionalId) {
        return jdbcTemplate.query("""
                select l.id, l.code, l.name, l.address, l.active
                from professional_location pl join clinic_location l on l.id = pl.location_id
                where pl.professional_id = ? order by l.code
                """, (resultSet, rowNumber) -> location(resultSet), uuidBytes(professionalId));
    }

    private List<Specialty> specialties(String baseSql, Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        return jdbcTemplate.query(baseSql + " and id in (" + placeholders + ")", (resultSet, rowNumber) -> specialty(resultSet),
                ids.toArray());
    }

    private List<ClinicLocation> locations(String baseSql, Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        return jdbcTemplate.query(baseSql + " and id in (" + placeholders + ")", (resultSet, rowNumber) -> location(resultSet),
                ids.toArray());
    }

    private Specialty specialty(ResultSet resultSet) throws SQLException {
        return new Specialty(resultSet.getLong("id"), resultSet.getString("code"), resultSet.getString("name"),
                resultSet.getInt("duration_minutes"), resultSet.getBoolean("active"));
    }

    private ClinicLocation location(ResultSet resultSet) throws SQLException {
        return new ClinicLocation(resultSet.getLong("id"), resultSet.getString("code"), resultSet.getString("name"),
                resultSet.getString("address"), resultSet.getBoolean("active"));
    }

    private byte[] uuidBytes(UUID value) {
        return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array();
    }

    private UUID uuid(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
