package co.academy.citas.application.service;

import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.application.port.out.ProfilePort;
import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.domain.account.UserAccount;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserAccountPort users;
    private final ProfilePort profiles;

    public CurrentUserService(UserAccountPort users, ProfilePort profiles) { this.users = users; this.profiles = profiles; }

    public UserAccount get(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User was not found"));
    }

    public UserAccount update(UUID userId, String givenNames, String familyNames, String email, String phone) {
        if (givenNames == null || givenNames.isBlank() || familyNames == null || familyNames.isBlank()
                || email == null || email.isBlank() || phone == null || phone.isBlank() || !email.contains("@"))
            throw new IllegalArgumentException("Profile fields are invalid");
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        users.findByEmail(normalizedEmail).filter(existing -> !existing.id().equals(userId))
                .ifPresent(existing -> { throw new EmailAlreadyRegisteredException(); });
        profiles.update(userId, givenNames.trim(), familyNames.trim(), normalizedEmail, phone.trim());
        return get(userId);
    }
}
