package ci.transit.system.transport.server.impl.persistence.payment;

import java.time.Instant;

import ci.transit.system.transport.server.impl.ennumerations.PaymentMethod;
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
 * Cette classe represente un moyen de paiement enregistre par un usager,
 * reutilisable pour financer ses abonnements sans le ressaisir a chaque fois.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "payment_accounts")
public class PaymentAccount extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(length = 100)
    private String label;

    @Column(nullable = false, length = 100)
    private String reference;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAccount = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
