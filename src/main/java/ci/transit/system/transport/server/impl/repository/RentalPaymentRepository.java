package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.rental.RentalPayment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RentalPaymentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<RentalPayment> findByRental(UUID rentalIdentifier) {
        return entityManager.createQuery(
            "SELECT p FROM RentalPayment p WHERE p.rental.uuid = :rental ORDER BY p.paidAt DESC",
            RentalPayment.class
        ).setParameter("rental", rentalIdentifier).getResultList();
    }

    public RentalPayment findById(UUID identifier) {
        return entityManager.find(RentalPayment.class, identifier);
    }

    @Transactional
    public RentalPayment save(RentalPayment payment) {
        if (payment.getUuid() == null) {
            entityManager.persist(payment);
            return payment;
        }
        return entityManager.merge(payment);
    }

    @Transactional
    public void delete(UUID identifier) {
        RentalPayment payment = entityManager.find(RentalPayment.class, identifier);
        if (payment != null) {
            entityManager.remove(payment);
        }
    }
}
