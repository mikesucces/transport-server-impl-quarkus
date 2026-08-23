package ci.transit.system.transport.server.impl.persistence.fleet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.DriverStatus;
import ci.transit.system.transport.server.impl.ennumerations.RemunerationType;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des chauffeurs.
 *
 * Le rattachement a un compte applicatif (Keycloak) est optionnel :
 * un chauffeur peut exister en base avant d'avoir un compte.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "drivers")
public class Driver extends BaseEntity {

    @Column(name = "staff_profile_id", unique = true)
    private UUID staffProfileIdentifier;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 30)
    private String phone;

    @Column(unique = true, length = 30)
    private String matricule;

    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "license_category", nullable = false, length = 10)
    private String licenseCategory = "D";

    @Column(name = "license_expires_on", nullable = false)
    private LocalDate licenseExpiresOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "remuneration_type", nullable = false, length = 20)
    private RemunerationType remunerationType = RemunerationType.FIXE;

    @Column(name = "monthly_salary", precision = 12, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "trip_rate", precision = 12, scale = 2)
    private BigDecimal tripRate;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.DISPONIBLE;

    @Column(name = "hired_on")
    private LocalDate hiredOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /** Nombre de jours avant expiration du permis. */
    @Transient
    public long getDaysUntilLicenseExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), licenseExpiresOn);
    }
}
