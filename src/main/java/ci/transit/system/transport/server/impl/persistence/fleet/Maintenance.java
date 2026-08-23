package ci.transit.system.transport.server.impl.persistence.fleet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import ci.transit.system.transport.server.impl.ennumerations.MaintenanceStatus;
import ci.transit.system.transport.server.impl.ennumerations.MaintenanceType;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des entretiens d'un car.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "maintenances")
public class Maintenance extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false, length = 30)
    private MaintenanceType maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceStatus status = MaintenanceStatus.PLANIFIE;

    @Column(name = "scheduled_on")
    private LocalDate scheduledOn;

    @Column(name = "performed_on")
    private LocalDate performedOn;

    @Column(name = "mileage_km")
    private Integer mileageKm;

    @Column(name = "next_due_km")
    private Integer nextDueKm;

    @Column(name = "next_due_on")
    private LocalDate nextDueOn;

    @Column(name = "cost_amount", precision = 12, scale = 2)
    private BigDecimal costAmount;

    @Column(length = 150)
    private String garage;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
