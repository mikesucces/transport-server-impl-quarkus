package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.PassengerStatus;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PassengerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Passenger> findAll() {
        return entityManager.createQuery(
            "SELECT p FROM Passenger p ORDER BY p.fullName", Passenger.class
        ).getResultList();
    }

    public List<Passenger> findByStatus(PassengerStatus status) {
        return entityManager.createQuery(
            "SELECT p FROM Passenger p WHERE p.status = :status ORDER BY p.fullName", Passenger.class
        ).setParameter("status", status).getResultList();
    }

    public Passenger findById(UUID identifier) {
        return entityManager.find(Passenger.class, identifier);
    }

    public Optional<Passenger> findByPhone(String phone) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT p FROM Passenger p WHERE p.phone = :phone", Passenger.class
            ).setParameter("phone", phone).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Passenger save(Passenger passenger) {
        if (passenger.getUuid() == null) {
            entityManager.persist(passenger);
            return passenger;
        }
        return entityManager.merge(passenger);
    }

    @Transactional
    public void delete(UUID identifier) {
        Passenger passenger = entityManager.find(Passenger.class, identifier);
        if (passenger != null) {
            entityManager.remove(passenger);
        }
    }
}
