package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.VehicleStatus;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VehicleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Vehicle> findAll() {
        return entityManager.createQuery(
            "SELECT v FROM Vehicle v ORDER BY v.plateNumber", Vehicle.class
        ).getResultList();
    }

    public List<Vehicle> findByStatus(VehicleStatus status) {
        return entityManager.createQuery(
            "SELECT v FROM Vehicle v WHERE v.status = :status ORDER BY v.plateNumber", Vehicle.class
        ).setParameter("status", status).getResultList();
    }

    public Vehicle findById(UUID identifier) {
        return entityManager.find(Vehicle.class, identifier);
    }

    public Optional<Vehicle> findByPlateNumber(String plateNumber) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT v FROM Vehicle v WHERE v.plateNumber = :plate", Vehicle.class
            ).setParameter("plate", plateNumber).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public long countByStatus(VehicleStatus status) {
        return entityManager.createQuery(
            "SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status", Long.class
        ).setParameter("status", status).getSingleResult();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(v) FROM Vehicle v", Long.class)
            .getSingleResult();
    }

    @Transactional
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getUuid() == null) {
            entityManager.persist(vehicle);
            return vehicle;
        }
        return entityManager.merge(vehicle);
    }

    @Transactional
    public void delete(UUID identifier) {
        Vehicle vehicle = entityManager.find(Vehicle.class, identifier);
        if (vehicle != null) {
            entityManager.remove(vehicle);
        }
    }
}
