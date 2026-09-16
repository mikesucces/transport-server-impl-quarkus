package ci.transit.system.transport.server.impl.persistence.rental;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.PaymentMethod;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import ci.transit.system.transport.server.impl.persistence.payment.PaymentAccount;
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
 * Cette classe represente l'entite des paiements de location : un
 * encaissement (acompte ou solde) lie a une location.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "rental_payments")
public class RentalPayment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    @ManyToOne
    @JoinColumn(name = "payment_account_id")
    private PaymentAccount paymentAccount;

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
