package co.academy.citas.domain.account;

import java.util.Set;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String givenNames,
        String familyNames,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        String passwordHash,
        Set<Role> roles) {
}
