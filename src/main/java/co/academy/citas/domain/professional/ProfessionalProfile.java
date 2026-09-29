package co.academy.citas.domain.professional;

import java.util.List;
import java.util.UUID;

public record ProfessionalProfile(UUID id, UUID userId, String givenNames, String familyNames, String email,
                                  String professionalCode, String licenseNumber, boolean active,
                                  List<SpecialtyAssignment> specialties, List<ClinicLocation> locations) {
    public record SpecialtyAssignment(Specialty specialty, boolean primary) {
    }
}
