package co.academy.citas.application.port.out;

import co.academy.citas.domain.professional.ClinicLocation;
import co.academy.citas.domain.professional.ProfessionalProfile;
import co.academy.citas.domain.professional.Specialty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalOfferPort {
    boolean existsByProfessionalCode(String professionalCode);
    boolean existsByLicenseNumber(String licenseNumber);
    ProfessionalProfile save(ProfessionalProfile profile);
    Optional<ProfessionalProfile> findById(UUID professionalId);
    List<ProfessionalProfile> findAll();
    List<Specialty> findActiveSpecialtiesByIds(Collection<Long> ids);
    List<Specialty> findActiveSpecialties();
    List<ClinicLocation> findLocationsByIds(Collection<Long> ids);
    List<ClinicLocation> findLocations();
    void replaceAssignments(UUID professionalId, Collection<Long> specialtyIds, long primarySpecialtyId,
                            Collection<Long> locationIds);
    void updateActive(UUID professionalId, boolean active);
}
