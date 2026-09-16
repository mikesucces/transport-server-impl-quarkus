package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.identity.AccessCode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AccessCodeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<AccessCode> findAll() {
        return entityManager.createQuery(
            "SELECT a FROM AccessCode a ORDER BY a.validUntil DESC", AccessCode.class
        ).getResultList();
    }

    public List<AccessCode> findByVehicle(UUID vehicleIdentifier) {
        return entityManager.createQuery(
            "SELECT a FROM AccessCode a WHERE a.vehicle.uuid = :vehicle ORDER BY a.validUntil DESC",
            AccessCode.class
        ).setParameter("vehicle", vehicleIdentifier).getResultList();
    }

    public AccessCode findById(UUID identifier) {
        return entityManager.find(AccessCode.class, identifier);
    }

    /** Codes actifs pour une rotation, ou a defaut pour son vehicule sans rotation associee. */
    public List<AccessCode> findActive(UUID rotationIdentifier, UUID vehicleIdentifier) {
        return entityManager.createQuery(
            "SELECT a FROM AccessCode a WHERE "
          + "(a.rotation.uuid = :rotation OR (a.rotation IS NULL AND a.vehicle.uuid = :vehicle)) "
          + "AND a.validFrom <= CURRENT_TIMESTAMP AND a.validUntil >= CURRENT_TIMESTAMP "
          + "ORDER BY a.validUntil DESC", AccessCode.class
        ).setParameter("rotation", rotationIdentifier)
         .setParameter("vehicle", vehicleIdentifier)
         .getResultList();
    }

    @Transactional
    public AccessCode save(AccessCode accessCode) {
        if (accessCode.getUuid() == null) {
            entityManager.persist(accessCode);
            return accessCode;
        }
        return entityManager.merge(accessCode);
    }

    @Transactional
    public void delete(UUID identifier) {
        AccessCode accessCode = entityManager.find(AccessCode.class, identifier);
        if (accessCode != null) {
            entityManager.remove(accessCode);
        }
    }
}
