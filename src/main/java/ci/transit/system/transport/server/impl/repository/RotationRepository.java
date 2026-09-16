package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.RotationStatus;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RotationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Rotation> findAll() {
        return entityManager.createQuery(
            "SELECT r FROM Rotation r ORDER BY r.scheduledStart DESC", Rotation.class
        ).getResultList();
    }

    public List<Rotation> findByStatus(RotationStatus status) {
        return entityManager.createQuery(
            "SELECT r FROM Rotation r WHERE r.status = :status ORDER BY r.scheduledStart DESC",
            Rotation.class
        ).setParameter("status", status).getResultList();
    }

    public List<Rotation> findByDriver(UUID driverIdentifier) {
        return entityManager.createQuery(
            "SELECT r FROM Rotation r WHERE r.driver.uuid = :driver ORDER BY r.scheduledStart DESC",
            Rotation.class
        ).setParameter("driver", driverIdentifier).getResultList();
    }

    public List<Rotation> findByVehicle(UUID vehicleIdentifier) {
        return entityManager.createQuery(
            "SELECT r FROM Rotation r WHERE r.vehicle.uuid = :vehicle ORDER BY r.scheduledStart DESC",
            Rotation.class
        ).setParameter("vehicle", vehicleIdentifier).getResultList();
    }

    public List<Rotation> findByRoute(UUID routeIdentifier) {
        return entityManager.createQuery(
            "SELECT r FROM Rotation r WHERE r.route.uuid = :route ORDER BY r.scheduledStart DESC",
            Rotation.class
        ).setParameter("route", routeIdentifier).getResultList();
    }

    public List<Rotation> findActive() {
        return entityManager.createQuery(
            "SELECT r FROM Rotation r WHERE r.status = :status ORDER BY r.scheduledStart", Rotation.class
        ).setParameter("status", RotationStatus.EN_COURS).getResultList();
    }

    public Rotation findById(UUID identifier) {
        return entityManager.find(Rotation.class, identifier);
    }

    @Transactional
    public Rotation save(Rotation rotation) {
        if (rotation.getUuid() == null) {
            entityManager.persist(rotation);
            return rotation;
        }
        return entityManager.merge(rotation);
    }

    @Transactional
    public void delete(UUID identifier) {
        Rotation rotation = entityManager.find(Rotation.class, identifier);
        if (rotation != null) {
            entityManager.remove(rotation);
        }
    }
}
