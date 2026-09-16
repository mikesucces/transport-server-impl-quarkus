package ci.transit.system.transport.server.impl.persistence.rotation;

import java.time.Instant;

import ci.transit.system.transport.server.impl.ennumerations.RotationStatus;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import ci.transit.system.transport.server.impl.persistence.fleet.Driver;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.route.Route;
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
 * Cette classe represente l'entite des rotations : l'affectation d'un
 * chauffeur a un vehicule pour une vacation (plage horaire).
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "rotations")
public class Rotation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RotationStatus status = RotationStatus.PLANIFIEE;

    @Column(name = "scheduled_start", nullable = false)
    private Instant scheduledStart;

    @Column(name = "scheduled_end")
    private Instant scheduledEnd;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "start_mileage_km")
    private Integer startMileageKm;

    @Column(name = "end_mileage_km")
    private Integer endMileageKm;

    @Column(name = "access_code_reference", length = 64)
    private String accessCodeReference;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
