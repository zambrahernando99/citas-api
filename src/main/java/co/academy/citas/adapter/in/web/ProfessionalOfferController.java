package co.academy.citas.adapter.in.web;

import co.academy.citas.application.port.in.AssignProfessionalOfferCommand;
import co.academy.citas.application.port.in.CreateProfessionalCommand;
import co.academy.citas.application.port.in.ProfessionalCatalogQuery;
import co.academy.citas.application.port.in.ProfessionalOfferUseCase;
import co.academy.citas.domain.professional.ClinicLocation;
import co.academy.citas.domain.professional.ProfessionalProfile;
import co.academy.citas.domain.professional.Specialty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProfessionalOfferController {
    private final ProfessionalOfferUseCase professionalOfferUseCase;
    private final ProfessionalCatalogQuery professionalCatalogQuery;

    public ProfessionalOfferController(ProfessionalOfferUseCase professionalOfferUseCase,
                                       ProfessionalCatalogQuery professionalCatalogQuery) {
        this.professionalOfferUseCase = professionalOfferUseCase;
        this.professionalCatalogQuery = professionalCatalogQuery;
    }

    @GetMapping("/catalogs/locations")
    List<LocationResponse> locations() {
        return professionalCatalogQuery.fixedLocations().stream().map(LocationResponse::from).toList();
    }

    @GetMapping("/specialties")
    List<SpecialtyResponse> specialties() {
        return professionalCatalogQuery.activeSpecialties().stream().map(SpecialtyResponse::from).toList();
    }

    @GetMapping("/admin/professionals")
    List<ProfessionalResponse> list() {
        return professionalOfferUseCase.list().stream().map(ProfessionalResponse::from).toList();
    }

    @GetMapping("/professionals")
    List<ProfessionalResponse> activeProfessionals() {
        return professionalOfferUseCase.list().stream().filter(ProfessionalProfile::active)
                .map(ProfessionalResponse::from).toList();
    }

    @PostMapping("/admin/professionals")
    ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody CreateProfessionalRequest request) {
        ProfessionalProfile professional = professionalOfferUseCase.create(new CreateProfessionalCommand(
                request.givenNames(), request.familyNames(), request.documentType(), request.documentNumber(),
                request.email(), request.phone(), request.temporaryPassword(), request.professionalCode(),
                request.licenseNumber()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfessionalResponse.from(professional));
    }

    @PutMapping("/admin/professionals/{professionalId}/assignments")
    ProfessionalResponse assign(@PathVariable UUID professionalId, @Valid @RequestBody AssignmentsRequest request) {
        return ProfessionalResponse.from(professionalOfferUseCase.assign(new AssignProfessionalOfferCommand(professionalId,
                request.specialtyIds(), request.primarySpecialtyId(), request.locationIds())));
    }

    @PatchMapping("/admin/professionals/{professionalId}/active")
    ProfessionalResponse active(@PathVariable UUID professionalId, @Valid @RequestBody ActiveRequest request) {
        return ProfessionalResponse.from(professionalOfferUseCase.changeActive(professionalId, request.active()));
    }

    public record CreateProfessionalRequest(@NotBlank String givenNames, @NotBlank String familyNames,
                                            @NotBlank String documentType, @NotBlank String documentNumber,
                                            @NotBlank @Email String email, @NotBlank String phone,
                                            @NotBlank String temporaryPassword, @NotBlank String professionalCode,
                                            @NotBlank String licenseNumber) {
    }

    public record AssignmentsRequest(@NotEmpty List<@NotNull Long> specialtyIds, @NotNull Long primarySpecialtyId,
                                     @NotEmpty @Size(max = 2) List<@NotNull Long> locationIds) {
    }

    public record ActiveRequest(boolean active) {
    }

    public record SpecialtyResponse(long id, String code, String name, int durationMinutes, boolean active) {
        static SpecialtyResponse from(Specialty specialty) {
            return new SpecialtyResponse(specialty.id(), specialty.code(), specialty.name(), specialty.durationMinutes(),
                    specialty.active());
        }
    }

    public record LocationResponse(long id, String code, String name, String address, boolean active) {
        static LocationResponse from(ClinicLocation location) {
            return new LocationResponse(location.id(), location.code(), location.name(), location.address(), location.active());
        }
    }

    public record ProfessionalResponse(UUID id, String givenNames, String familyNames, String email,
                                       String professionalCode, String licenseNumber, boolean active,
                                       List<SpecialtyAssignmentResponse> specialties, List<LocationResponse> locations) {
        static ProfessionalResponse from(ProfessionalProfile profile) {
            return new ProfessionalResponse(profile.id(), profile.givenNames(), profile.familyNames(), profile.email(),
                    profile.professionalCode(), profile.licenseNumber(), profile.active(), profile.specialties().stream()
                    .map(assignment -> new SpecialtyAssignmentResponse(SpecialtyResponse.from(assignment.specialty()),
                            assignment.primary())).toList(), profile.locations().stream().map(LocationResponse::from).toList());
        }
    }

    public record SpecialtyAssignmentResponse(SpecialtyResponse specialty, boolean primary) {
    }
}
