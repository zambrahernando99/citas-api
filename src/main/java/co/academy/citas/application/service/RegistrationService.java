package co.academy.citas.application.service;

import co.academy.citas.application.exception.DocumentAlreadyRegisteredException;
import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.application.port.in.RegisterUserCommand;
import co.academy.citas.application.port.in.RegisterUserUseCase;
import co.academy.citas.application.port.out.PasswordHashingPort;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.domain.account.Role;
import co.academy.citas.domain.account.UserAccount;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class RegistrationService implements RegisterUserUseCase {
    private final UserAccountPort userAccountPort;
    private final PasswordHashingPort passwordHashingPort;

    public RegistrationService(UserAccountPort userAccountPort, PasswordHashingPort passwordHashingPort) {
        this.userAccountPort = userAccountPort;
        this.passwordHashingPort = passwordHashingPort;
    }

    @Override
    public UserAccount register(RegisterUserCommand command) {
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        String documentNumber = command.documentNumber().trim();
        if (userAccountPort.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        if (userAccountPort.findByDocumentNumber(documentNumber).isPresent()) {
            throw new DocumentAlreadyRegisteredException();
        }

        UserAccount user = new UserAccount(
                UUID.randomUUID(),
                command.givenNames().trim(),
                command.familyNames().trim(),
                command.documentType().trim(),
                documentNumber,
                email,
                command.phone().trim(),
                passwordHashingPort.hash(command.password()),
                Set.of(Role.USER));
        return userAccountPort.save(user);
    }
}
