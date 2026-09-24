package co.academy.citas.application.port.in;

import java.util.List;
import java.util.UUID;

public record AssignProfessionalOfferCommand(UUID professionalId, List<Long> specialtyIds, long primarySpecialtyId,
                                             List<Long> locationIds) {
}
