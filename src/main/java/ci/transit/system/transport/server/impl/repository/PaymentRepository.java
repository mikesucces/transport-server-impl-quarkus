package ci.transit.system.transport.server.impl.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.subscription.Payment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PaymentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Payment> findAll() {
        return entityManager.createQuery(
            "SELECT p FROM Payment p ORDER BY p.paidAt DESC", Payment.class
        ).getResultList();
    }

    public List<Payment> findBySubscription(UUID subscriptionIdentifier) {
        return entityManager.createQuery(
            "SELECT p FROM Payment p WHERE p.subscription.uuid = :subscription ORDER BY p.paidAt DESC",
            Payment.class
        ).setParameter("subscription", subscriptionIdentifier).getResultList();
    }

    /** Somme des encaissements depuis une date donnee (ex. debut du mois). */
    public BigDecimal sumAmountSince(Instant since) {
        BigDecimal total = entityManager.createQuery(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paidAt >= :since", BigDecimal.class
        ).setParameter("since", since).getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    public Payment findById(UUID identifier) {
        return entityManager.find(Payment.class, identifier);
    }

    @Transactional
    public Payment save(Payment payment) {
        if (payment.getUuid() == null) {
            entityManager.persist(payment);
            return payment;
        }
        return entityManager.merge(payment);
    }

    @Transactional
    public void delete(UUID identifier) {
        Payment payment = entityManager.find(Payment.class, identifier);
        if (payment != null) {
            entityManager.remove(payment);
        }
    }
}
