package co.academy.citas.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.application.port.in.RegisterUserCommand;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.domain.account.Role;
import co.academy.citas.domain.account.UserAccount;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistrationServiceTest {
    @Test
    void assignsOnlyUserAndHashesThePassword() {
        InMemoryUsers users = new InMemoryUsers();
        RegistrationService service = new RegistrationService(users, new FakePasswordHasher());

        UserAccount result = service.register(command("ana@example.test", "100"));

        assertThat(result.roles()).containsExactly(Role.USER);
        assertThat(result.passwordHash()).isEqualTo("adaptive-hash");
        assertThat(result.passwordHash()).isNotEqualTo("synthetic-password");
    }

    @Test
    void rejectsDuplicateEmailBeforePersistingAnotherAccount() {
        InMemoryUsers users = new InMemoryUsers();
        RegistrationService service = new RegistrationService(users, new FakePasswordHasher());
        service.register(command("ana@example.test", "100"));

        assertThatThrownBy(() -> service.register(command("ana@example.test", "200")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        assertThat(users.byId).hasSize(1);
    }

    private RegisterUserCommand command(String email, String document) {
        return new RegisterUserCommand("Ana", "Prueba", "CC", document, email, "3000000000", "synthetic-password");
    }

    private static final class FakePasswordHasher implements PasswordHashingPort {
        @Override public String hash(String rawPassword) { return "adaptive-hash"; }
        @Override public boolean matches(String rawPassword, String passwordHash) { return false; }
    }

    private static final class InMemoryUsers implements UserAccountPort {
        private final Map<UUID, UserAccount> byId = new HashMap<>();

        @Override public Optional<UserAccount> findByEmail(String email) {
            return byId.values().stream().filter(user -> user.email().equals(email)).findFirst();
        }
        @Override public Optional<UserAccount> findByDocumentNumber(String documentNumber) {
            return byId.values().stream().filter(user -> user.documentNumber().equals(documentNumber)).findFirst();
        }
        @Override public Optional<UserAccount> findById(UUID id) { return Optional.ofNullable(byId.get(id)); }
        @Override public UserAccount save(UserAccount userAccount) {
            byId.put(userAccount.id(), userAccount);
            return userAccount;
        }
    }
}
