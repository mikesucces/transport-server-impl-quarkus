package ci.transit.system.transport.server.impl.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.fleet.VehicleDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VehicleDocumentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<VehicleDocument> findByVehicle(UUID vehicleIdentifier) {
        return entityManager.createQuery(
            "SELECT d FROM VehicleDocument d WHERE d.vehicle.uuid = :vehicle ORDER BY d.expiresOn",
            VehicleDocument.class
        ).setParameter("vehicle", vehicleIdentifier).getResultList();
    }

    /** Documents deja expires ou arrivant a echeance avant la date limite. */
    public List<VehicleDocument> findExpiringBefore(LocalDate limit) {
        return entityManager.createQuery(
            "SELECT d FROM VehicleDocument d WHERE d.expiresOn <= :limit ORDER BY d.expiresOn",
            VehicleDocument.class
        ).setParameter("limit", limit).getResultList();
    }

    public VehicleDocument findById(UUID identifier) {
        return entityManager.find(VehicleDocument.class, identifier);
    }

    @Transactional
    public VehicleDocument save(VehicleDocument document) {
        if (document.getUuid() == null) {
            entityManager.persist(document);
            return document;
        }
        return entityManager.merge(document);
    }

    @Transactional
    public void delete(UUID identifier) {
        VehicleDocument document = entityManager.find(VehicleDocument.class, identifier);
        if (document != null) {
            entityManager.remove(document);
        }
    }
}
