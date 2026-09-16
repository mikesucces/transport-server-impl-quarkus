package ci.transit.system.transport.server.impl.persistence.subscription;

import java.time.Instant;
import java.time.LocalDate;

import ci.transit.system.transport.server.impl.ennumerations.SubscriptionPlan;
import ci.transit.system.transport.server.impl.ennumerations.SubscriptionStatus;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
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
 * Cette classe represente l'entite des abonnements usagers : une periode
 * d'acces au transport, financee par un ou plusieurs paiements.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on", nullable = false)
    private LocalDate endsOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
