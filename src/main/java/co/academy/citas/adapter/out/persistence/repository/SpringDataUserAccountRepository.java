package co.academy.citas.adapter.out.persistence.repository;

import co.academy.citas.adapter.out.persistence.entity.UserAccountJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserAccountRepository extends JpaRepository<UserAccountJpaEntity, UUID> {
    Optional<UserAccountJpaEntity> findByEmail(String email);
    Optional<UserAccountJpaEntity> findByDocumentNumber(String documentNumber);
}
