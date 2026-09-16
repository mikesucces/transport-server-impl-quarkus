package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.StaffType;
import ci.transit.system.transport.server.impl.persistence.staff.StaffProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StaffProfileRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<StaffProfile> findAll() {
        return entityManager.createQuery(
            "SELECT s FROM StaffProfile s ORDER BY s.fullName", StaffProfile.class
        ).getResultList();
    }

    public List<StaffProfile> findByStaffType(StaffType staffType) {
        return entityManager.createQuery(
            "SELECT s FROM StaffProfile s WHERE s.staffType = :type ORDER BY s.fullName", StaffProfile.class
        ).setParameter("type", staffType).getResultList();
    }

    public Optional<StaffProfile> findByKeycloakSub(UUID keycloakSub) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT s FROM StaffProfile s WHERE s.keycloakSub = :sub", StaffProfile.class
            ).setParameter("sub", keycloakSub).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<StaffProfile> findByMatricule(String matricule) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT s FROM StaffProfile s WHERE s.matricule = :matricule", StaffProfile.class
            ).setParameter("matricule", matricule).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public StaffProfile findById(UUID identifier) {
        return entityManager.find(StaffProfile.class, identifier);
    }

    @Transactional
    public StaffProfile save(StaffProfile staffProfile) {
        if (staffProfile.getUuid() == null) {
            entityManager.persist(staffProfile);
            return staffProfile;
        }
        return entityManager.merge(staffProfile);
    }

    @Transactional
    public void delete(UUID identifier) {
        StaffProfile staffProfile = entityManager.find(StaffProfile.class, identifier);
        if (staffProfile != null) {
            entityManager.remove(staffProfile);
        }
    }
}
