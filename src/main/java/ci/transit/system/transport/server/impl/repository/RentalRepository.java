package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.RentalStatus;
import ci.transit.system.transport.server.impl.persistence.rental.Rental;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RentalRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Rental> findAll() {
        return entityManager.createQuery(
            "SELECT r FROM Rental r ORDER BY r.startDate DESC", Rental.class
        ).getResultList();
    }

    public List<Rental> findByStatus(RentalStatus status) {
        return entityManager.createQuery(
            "SELECT r FROM Rental r WHERE r.status = :status ORDER BY r.startDate DESC", Rental.class
        ).setParameter("status", status).getResultList();
    }

    public List<Rental> findByPassenger(UUID passengerIdentifier) {
        return entityManager.createQuery(
            "SELECT r FROM Rental r WHERE r.passenger.uuid = :passenger ORDER BY r.startDate DESC",
            Rental.class
        ).setParameter("passenger", passengerIdentifier).getResultList();
    }

    public List<Rental> findByVehicle(UUID vehicleIdentifier) {
        return entityManager.createQuery(
            "SELECT r FROM Rental r WHERE r.vehicle.uuid = :vehicle ORDER BY r.startDate DESC",
            Rental.class
        ).setParameter("vehicle", vehicleIdentifier).getResultList();
    }

    public List<Rental> findActive() {
        return entityManager.createQuery(
            "SELECT r FROM Rental r WHERE r.status = :status ORDER BY r.startDate", Rental.class
        ).setParameter("status", RentalStatus.EN_COURS).getResultList();
    }

    public Rental findById(UUID identifier) {
        return entityManager.find(Rental.class, identifier);
    }

    @Transactional
    public Rental save(Rental rental) {
        if (rental.getUuid() == null) {
            entityManager.persist(rental);
            return rental;
        }
        return entityManager.merge(rental);
    }

    @Transactional
    public void delete(UUID identifier) {
        Rental rental = entityManager.find(Rental.class, identifier);
        if (rental != null) {
            entityManager.remove(rental);
        }
    }
}
