package ci.transit.system.transport.server.impl.persistence.identity;

import java.time.Instant;

import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des codes d'acces temporaires des
 * usagers. Le code est lie au car, pas a l'usager : changer de car
 * necessite un nouveau code (le numero de car est donc conserve tel
 * quel au moment de la creation, meme si le car est renomme ensuite).
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "access_codes")
public class AccessCode extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "vehicle_number", nullable = false, length = 30)
    private String vehicleNumber;

    @ManyToOne
    @JoinColumn(name = "rotation_id")
    private Rotation rotation;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /** Le code est-il valide a l'instant present. */
    @Transient
    public boolean isActive() {
        Instant now = Instant.now();
        return !now.isBefore(validFrom) && !now.isAfter(validUntil);
    }
}
