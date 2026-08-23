package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.identity.LoginAttempt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LoginAttemptRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<LoginAttempt> findAll() {
        return entityManager.createQuery(
            "SELECT l FROM LoginAttempt l ORDER BY l.attemptedAt DESC", LoginAttempt.class
        ).getResultList();
    }

    public List<LoginAttempt> findByPassenger(UUID passengerIdentifier) {
        return entityManager.createQuery(
            "SELECT l FROM LoginAttempt l WHERE l.passenger.uuid = :passenger ORDER BY l.attemptedAt DESC",
            LoginAttempt.class
        ).setParameter("passenger", passengerIdentifier).getResultList();
    }

    public LoginAttempt findById(Long identifier) {
        return entityManager.find(LoginAttempt.class, identifier);
    }

    @Transactional
    public LoginAttempt save(LoginAttempt loginAttempt) {
        if (loginAttempt.getId() == null) {
            entityManager.persist(loginAttempt);
            return loginAttempt;
        }
        return entityManager.merge(loginAttempt);
    }

    @Transactional
    public void delete(Long identifier) {
        LoginAttempt loginAttempt = entityManager.find(LoginAttempt.class, identifier);
        if (loginAttempt != null) {
            entityManager.remove(loginAttempt);
        }
    }
}
