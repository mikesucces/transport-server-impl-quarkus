package ci.transit.system.transport.server.impl.persistence.route;

import java.time.Instant;

import ci.transit.system.transport.server.impl.ennumerations.RouteStatus;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des lignes : un trajet fixe (point A
 * vers point B) auquel des rotations peuvent etre rattachees.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "routes")
public class Route extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String origin;

    @Column(nullable = false, length = 150)
    private String destination;

    @Column(name = "distance_km")
    private Integer distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStatus status = RouteStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
