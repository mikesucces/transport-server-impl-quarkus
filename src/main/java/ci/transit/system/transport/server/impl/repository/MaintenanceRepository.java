package ci.transit.system.transport.server.impl.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.MaintenanceStatus;
import ci.transit.system.transport.server.impl.persistence.fleet.Maintenance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MaintenanceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Maintenance> findByVehicle(UUID vehicleIdentifier) {
        return entityManager.createQuery(
            "SELECT m FROM Maintenance m WHERE m.vehicle.uuid = :vehicle "
          + "ORDER BY COALESCE(m.performedOn, m.scheduledOn) DESC", Maintenance.class
        ).setParameter("vehicle", vehicleIdentifier).getResultList();
    }

    /** Entretiens planifies ou en cours prevus avant la date limite. */
    public List<Maintenance> findUpcomingBefore(LocalDate limit) {
        return entityManager.createQuery(
            "SELECT m FROM Maintenance m WHERE m.status IN :statuses "
          + "AND m.scheduledOn <= :limit ORDER BY m.scheduledOn", Maintenance.class
        ).setParameter("statuses", List.of(MaintenanceStatus.PLANIFIE, MaintenanceStatus.EN_COURS))
         .setParameter("limit", limit)
         .getResultList();
    }

    public Maintenance findById(UUID identifier) {
        return entityManager.find(Maintenance.class, identifier);
    }

    @Transactional
    public Maintenance save(Maintenance maintenance) {
        if (maintenance.getUuid() == null) {
            entityManager.persist(maintenance);
            return maintenance;
        }
        return entityManager.merge(maintenance);
    }

    @Transactional
    public void delete(UUID identifier) {
        Maintenance maintenance = entityManager.find(Maintenance.class, identifier);
        if (maintenance != null) {
            entityManager.remove(maintenance);
        }
    }
}
