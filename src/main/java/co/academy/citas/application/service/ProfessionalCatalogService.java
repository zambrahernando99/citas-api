package co.academy.citas.application.service;

import co.academy.citas.application.port.in.ProfessionalCatalogQuery;
import co.academy.citas.application.port.out.ProfessionalOfferPort;
import co.academy.citas.domain.professional.ClinicLocation;
import co.academy.citas.domain.professional.Specialty;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class ProfessionalCatalogService implements ProfessionalCatalogQuery {
    private final ProfessionalOfferPort professionalOfferPort;

    public ProfessionalCatalogService(ProfessionalOfferPort professionalOfferPort) {
        this.professionalOfferPort = professionalOfferPort;
    }

    @Override public List<Specialty> activeSpecialties() { return professionalOfferPort.findActiveSpecialties(); }
    @Override public List<ClinicLocation> fixedLocations() { return professionalOfferPort.findLocations(); }
}
