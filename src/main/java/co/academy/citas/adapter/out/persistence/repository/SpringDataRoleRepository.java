package co.academy.citas.adapter.out.persistence.repository;

import co.academy.citas.adapter.out.persistence.entity.RoleJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByCode(String code);
}
