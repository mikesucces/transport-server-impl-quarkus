package ci.transit.system.transport.server.impl.persistence.staff;

import java.time.Instant;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.StaffType;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente le miroir metier d'un compte du personnel.
 * Keycloak reste la source de verite des identifiants/mots de passe :
 * cette entite ne porte que les donnees metier (matricule, rattachement).
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "staff_profiles")
public class StaffProfile extends BaseEntity {

    @Column(name = "keycloak_sub", nullable = false, unique = true)
    private UUID keycloakSub;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false, length = 20)
    private StaffType staffType;

    @Column(unique = true, length = 30)
    private String matricule;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt = Instant.now();

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
