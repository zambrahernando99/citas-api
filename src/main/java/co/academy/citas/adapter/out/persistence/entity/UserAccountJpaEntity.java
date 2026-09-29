package co.academy.citas.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_account")
public class UserAccountJpaEntity {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "given_names", nullable = false)
    private String givenNames;
    @Column(name = "family_names", nullable = false)
    private String familyNames;
    @Column(name = "document_type", nullable = false)
    private String documentType;
    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String phone;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_account_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleJpaEntity> roles = new HashSet<>();

    protected UserAccountJpaEntity() {
    }

    public UserAccountJpaEntity(UUID id, String givenNames, String familyNames, String documentType,
                                String documentNumber, String email, String phone, String passwordHash,
                                Set<RoleJpaEntity> roles) {
        this.id = id;
        this.givenNames = givenNames;
        this.familyNames = familyNames;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
    }

    public UUID getId() { return id; }
    public String getGivenNames() { return givenNames; }
    public String getFamilyNames() { return familyNames; }
    public String getDocumentType() { return documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public Set<RoleJpaEntity> getRoles() { return roles; }
}
