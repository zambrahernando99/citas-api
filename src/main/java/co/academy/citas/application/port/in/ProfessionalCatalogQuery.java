package co.academy.citas.application.port.in;

import co.academy.citas.domain.professional.ClinicLocation;
import co.academy.citas.domain.professional.Specialty;
import java.util.List;

public interface ProfessionalCatalogQuery {
    List<Specialty> activeSpecialties();
    List<ClinicLocation> fixedLocations();
}
