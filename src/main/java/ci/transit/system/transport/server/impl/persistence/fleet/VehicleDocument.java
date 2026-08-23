package ci.transit.system.transport.server.impl.persistence.fleet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import ci.transit.system.transport.server.impl.ennumerations.DocumentType;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des documents d'un car
 * (assurance, visite technique, carte grise).
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "vehicle_documents")
public class VehicleDocument extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 30)
    private DocumentType docType;

    @Column(length = 80)
    private String reference;

    @Column(length = 120)
    private String issuer;

    @Column(name = "issued_on")
    private LocalDate issuedOn;

    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /** Nombre de jours avant echeance (negatif si deja expire). */
    @Transient
    public long getDaysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiresOn);
    }

    @Transient
    public boolean isExpired() {
        return expiresOn.isBefore(LocalDate.now());
    }
}
