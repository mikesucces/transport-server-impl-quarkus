package ci.transit.system.transport.server.impl.persistence.attendance;

import java.time.Instant;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import ci.transit.system.transport.server.impl.persistence.identity.AccessCode;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des presences validees a l'embarquement :
 * un usager dont le code d'acces a ete verifie avec succes pour une rotation.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "attendances")
public class Attendance extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "rotation_id")
    private Rotation rotation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "access_code_id")
    private AccessCode accessCode;

    @Column(name = "controller_id")
    private UUID controllerId;

    @Column(name = "boarded_at", nullable = false)
    private Instant boardedAt = Instant.now();
}
