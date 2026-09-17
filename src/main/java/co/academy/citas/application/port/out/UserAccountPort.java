package co.academy.citas.application.port.out;

import co.academy.citas.domain.account.UserAccount;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountPort {
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByDocumentNumber(String documentNumber);
    Optional<UserAccount> findById(UUID id);
    UserAccount save(UserAccount userAccount);
}
