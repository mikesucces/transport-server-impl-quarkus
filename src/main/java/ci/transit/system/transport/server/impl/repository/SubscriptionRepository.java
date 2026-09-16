package ci.transit.system.transport.server.impl.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.SubscriptionStatus;
import ci.transit.system.transport.server.impl.persistence.subscription.Subscription;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SubscriptionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Subscription> findAll() {
        return entityManager.createQuery(
            "SELECT s FROM Subscription s ORDER BY s.endsOn DESC", Subscription.class
        ).getResultList();
    }

    public List<Subscription> findByPassenger(UUID passengerIdentifier) {
        return entityManager.createQuery(
            "SELECT s FROM Subscription s WHERE s.passenger.uuid = :passenger ORDER BY s.endsOn DESC",
            Subscription.class
        ).setParameter("passenger", passengerIdentifier).getResultList();
    }

    public List<Subscription> findByStatus(SubscriptionStatus status) {
        return entityManager.createQuery(
            "SELECT s FROM Subscription s WHERE s.status = :status ORDER BY s.endsOn DESC",
            Subscription.class
        ).setParameter("status", status).getResultList();
    }

    /** Abonnement actif d'un usager couvrant la date donnee, s'il existe. */
    public Optional<Subscription> findActiveForPassenger(UUID passengerIdentifier, LocalDate today) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT s FROM Subscription s WHERE s.passenger.uuid = :passenger "
              + "AND s.status = :status AND s.startsOn <= :today AND s.endsOn >= :today "
              + "ORDER BY s.endsOn DESC", Subscription.class
            ).setParameter("passenger", passengerIdentifier)
             .setParameter("status", SubscriptionStatus.ACTIVE)
             .setParameter("today", today)
             .setMaxResults(1)
             .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Subscription findById(UUID identifier) {
        return entityManager.find(Subscription.class, identifier);
    }

    @Transactional
    public Subscription save(Subscription subscription) {
        if (subscription.getUuid() == null) {
            entityManager.persist(subscription);
            return subscription;
        }
        return entityManager.merge(subscription);
    }

    @Transactional
    public void delete(UUID identifier) {
        Subscription subscription = entityManager.find(Subscription.class, identifier);
        if (subscription != null) {
            entityManager.remove(subscription);
        }
    }
}
