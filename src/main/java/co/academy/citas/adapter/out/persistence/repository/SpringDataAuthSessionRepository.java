package co.academy.citas.adapter.out.persistence.repository;

import co.academy.citas.adapter.out.persistence.entity.AuthSessionJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAuthSessionRepository extends JpaRepository<AuthSessionJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from AuthSessionJpaEntity session where session.id = :id")
    Optional<AuthSessionJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
