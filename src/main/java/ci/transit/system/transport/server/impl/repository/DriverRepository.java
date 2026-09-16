package ci.transit.system.transport.server.impl.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.DriverStatus;
import ci.transit.system.transport.server.impl.ennumerations.RemunerationType;
import ci.transit.system.transport.server.impl.persistence.fleet.Driver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DriverRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Driver> findAll() {
        return entityManager.createQuery(
            "SELECT d FROM Driver d ORDER BY d.fullName", Driver.class
        ).getResultList();
    }

    public List<Driver> findByStatus(DriverStatus status) {
        return entityManager.createQuery(
            "SELECT d FROM Driver d WHERE d.status = :status ORDER BY d.fullName", Driver.class
        ).setParameter("status", status).getResultList();
    }

    /** Chauffeurs dont le permis expire avant la date limite. */
    public List<Driver> findWithLicenseExpiringBefore(LocalDate limit) {
        return entityManager.createQuery(
            "SELECT d FROM Driver d WHERE d.licenseExpiresOn <= :limit ORDER BY d.licenseExpiresOn",
            Driver.class
        ).setParameter("limit", limit).getResultList();
    }

    public Driver findById(UUID identifier) {
        return entityManager.find(Driver.class, identifier);
    }

    public Optional<Driver> findByPhone(String phone) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT d FROM Driver d WHERE d.phone = :phone", Driver.class
            ).setParameter("phone", phone).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<Driver> findByStaffProfile(UUID staffProfileIdentifier) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT d FROM Driver d WHERE d.staffProfileIdentifier = :staffProfile", Driver.class
            ).setParameter("staffProfile", staffProfileIdentifier).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /** Masse salariale mensuelle des chauffeurs remuneres au fixe. */
    public BigDecimal sumMonthlySalary() {
        BigDecimal total = entityManager.createQuery(
            "SELECT COALESCE(SUM(d.monthlySalary), 0) FROM Driver d "
          + "WHERE d.remunerationType = :type AND d.status <> :suspended", BigDecimal.class
        ).setParameter("type", RemunerationType.FIXE)
         .setParameter("suspended", DriverStatus.SUSPENDU)
         .getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional
    public Driver save(Driver driver) {
        if (driver.getUuid() == null) {
            entityManager.persist(driver);
            return driver;
        }
        return entityManager.merge(driver);
    }

    @Transactional
    public void delete(UUID identifier) {
        Driver driver = entityManager.find(Driver.class, identifier);
        if (driver != null) {
            entityManager.remove(driver);
        }
    }
}
