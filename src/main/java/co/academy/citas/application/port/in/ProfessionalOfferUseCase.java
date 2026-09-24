package co.academy.citas.application.port.in;

import co.academy.citas.domain.professional.ProfessionalProfile;
import java.util.List;
import java.util.UUID;

public interface ProfessionalOfferUseCase {
    ProfessionalProfile create(CreateProfessionalCommand command);
    ProfessionalProfile assign(AssignProfessionalOfferCommand command);
    ProfessionalProfile changeActive(UUID professionalId, boolean active);
    List<ProfessionalProfile> list();
}
