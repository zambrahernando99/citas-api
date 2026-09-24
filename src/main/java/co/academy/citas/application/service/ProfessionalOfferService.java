package co.academy.citas.application.service;

import co.academy.citas.application.exception.DocumentAlreadyRegisteredException;
import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.application.exception.LicenseNumberAlreadyRegisteredException;
import co.academy.citas.application.exception.ProfessionalCodeAlreadyRegisteredException;
import co.academy.citas.application.exception.ProfessionalNotFoundException;
import co.academy.citas.application.port.in.AssignProfessionalOfferCommand;
import co.academy.citas.application.port.in.CreateProfessionalCommand;
import co.academy.citas.application.port.in.ProfessionalOfferUseCase;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.ProfessionalOfferPort;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.domain.account.Role;
import co.academy.citas.domain.account.UserAccount;
import co.academy.citas.domain.professional.ProfessionalProfile;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProfessionalOfferService implements ProfessionalOfferUseCase {
    private final UserAccountPort userAccountPort;
    private final ProfessionalOfferPort professionalOfferPort;
    private final PasswordHashingPort passwordHashingPort;

    public ProfessionalOfferService(UserAccountPort userAccountPort, ProfessionalOfferPort professionalOfferPort,
                                    PasswordHashingPort passwordHashingPort) {
        this.userAccountPort = userAccountPort;
        this.professionalOfferPort = professionalOfferPort;
        this.passwordHashingPort = passwordHashingPort;
    }

    @Override
    public ProfessionalProfile create(CreateProfessionalCommand command) {
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        String documentNumber = command.documentNumber().trim();
        String professionalCode = command.professionalCode().trim();
        String licenseNumber = command.licenseNumber().trim();
        if (userAccountPort.findByEmail(email).isPresent()) throw new EmailAlreadyRegisteredException();
        if (userAccountPort.findByDocumentNumber(documentNumber).isPresent()) throw new DocumentAlreadyRegisteredException();
        if (professionalOfferPort.existsByProfessionalCode(professionalCode)) throw new ProfessionalCodeAlreadyRegisteredException();
        if (professionalOfferPort.existsByLicenseNumber(licenseNumber)) throw new LicenseNumberAlreadyRegisteredException();

        UserAccount user = userAccountPort.save(new UserAccount(UUID.randomUUID(), command.givenNames().trim(),
                command.familyNames().trim(), command.documentType().trim(), documentNumber, email, command.phone().trim(),
                passwordHashingPort.hash(command.temporaryPassword()), Set.of(Role.PROFESSIONAL)));
        return professionalOfferPort.save(new ProfessionalProfile(UUID.randomUUID(), user.id(), user.givenNames(),
                user.familyNames(), user.email(), professionalCode, licenseNumber, true, List.of(), List.of()));
    }

    @Override
    public ProfessionalProfile assign(AssignProfessionalOfferCommand command) {
        ProfessionalProfile existing = professionalOfferPort.findById(command.professionalId())
                .orElseThrow(ProfessionalNotFoundException::new);
        Set<Long> specialtyIds = Set.copyOf(command.specialtyIds());
        Set<Long> locationIds = Set.copyOf(command.locationIds());
        if (specialtyIds.isEmpty() || specialtyIds.size() != command.specialtyIds().size()
                || !specialtyIds.contains(command.primarySpecialtyId())
                || professionalOfferPort.findActiveSpecialtiesByIds(specialtyIds).size() != specialtyIds.size()) {
            throw new IllegalArgumentException("Specialties must be active, assigned once, and include one primary specialty");
        }
        if (locationIds.isEmpty() || locationIds.size() > 2 || locationIds.size() != command.locationIds().size()
                || professionalOfferPort.findLocationsByIds(locationIds).size() != locationIds.size()) {
            throw new IllegalArgumentException("One or two fixed active locations must be assigned without duplicates");
        }
        professionalOfferPort.replaceAssignments(existing.id(), specialtyIds, command.primarySpecialtyId(), locationIds);
        return professionalOfferPort.findById(existing.id()).orElseThrow();
    }

    @Override
    public ProfessionalProfile changeActive(UUID professionalId, boolean active) {
        ProfessionalProfile existing = professionalOfferPort.findById(professionalId)
                .orElseThrow(ProfessionalNotFoundException::new);
        professionalOfferPort.updateActive(existing.id(), active);
        return professionalOfferPort.findById(existing.id()).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalProfile> list() {
        return professionalOfferPort.findAll();
    }
}
