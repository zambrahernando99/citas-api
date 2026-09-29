package co.academy.citas.application.port.in;

import co.academy.citas.domain.account.UserAccount;

public interface RegisterUserUseCase {
    UserAccount register(RegisterUserCommand command);
}
