package ci.transit.system.transport.server.impl.persistence.subscription;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.PaymentMethod;
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
 * Cette classe represente l'entite des paiements : un encaissement qui
 * finance (cree ou renouvelle) un abonnement usager.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "collected_by")
    private UUID collectedBy;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt = Instant.now();
}
