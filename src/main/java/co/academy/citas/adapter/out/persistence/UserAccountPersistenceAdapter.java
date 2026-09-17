package co.academy.citas.adapter.out.persistence;

import co.academy.citas.adapter.out.persistence.entity.RoleJpaEntity;
import co.academy.citas.adapter.out.persistence.entity.UserAccountJpaEntity;
import co.academy.citas.adapter.out.persistence.repository.SpringDataRoleRepository;
import co.academy.citas.adapter.out.persistence.repository.SpringDataUserAccountRepository;
import co.academy.citas.application.exception.DocumentAlreadyRegisteredException;
import co.academy.citas.application.exception.EmailAlreadyRegisteredException;
import co.academy.citas.application.port.out.UserAccountPort;
import co.academy.citas.domain.account.Role;
import co.academy.citas.domain.account.UserAccount;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class UserAccountPersistenceAdapter implements UserAccountPort {
    private final SpringDataUserAccountRepository userRepository;
    private final SpringDataRoleRepository roleRepository;

    public UserAccountPersistenceAdapter(SpringDataUserAccountRepository userRepository,
                                         SpringDataRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public java.util.Optional<UserAccount> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public java.util.Optional<UserAccount> findByDocumentNumber(String documentNumber) {
        return userRepository.findByDocumentNumber(documentNumber).map(this::toDomain);
    }

    @Override
    public java.util.Optional<UserAccount> findById(UUID id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        Set<RoleJpaEntity> roles = userAccount.roles().stream()
                .map(role -> roleRepository.findByCode(role.name())
                        .orElseThrow(() -> new IllegalStateException("Fixed role is missing")))
                .collect(Collectors.toUnmodifiableSet());
        try {
            return toDomain(userRepository.saveAndFlush(new UserAccountJpaEntity(
                    userAccount.id(), userAccount.givenNames(), userAccount.familyNames(),
                    userAccount.documentType(), userAccount.documentNumber(), userAccount.email(),
                    userAccount.phone(), userAccount.passwordHash(), roles)));
        } catch (DataIntegrityViolationException exception) {
            if (userRepository.findByEmail(userAccount.email()).isPresent()) {
                throw new EmailAlreadyRegisteredException();
            }
            throw new DocumentAlreadyRegisteredException();
        }
    }

    private UserAccount toDomain(UserAccountJpaEntity entity) {
        Set<Role> roles = entity.getRoles().stream()
                .map(role -> Role.valueOf(role.getCode()))
                .collect(Collectors.toUnmodifiableSet());
        return new UserAccount(entity.getId(), entity.getGivenNames(), entity.getFamilyNames(),
                entity.getDocumentType(), entity.getDocumentNumber(), entity.getEmail(), entity.getPhone(),
                entity.getPasswordHash(), roles);
    }
}
