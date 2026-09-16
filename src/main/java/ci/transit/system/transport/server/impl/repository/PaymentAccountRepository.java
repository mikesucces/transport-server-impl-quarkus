package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.payment.PaymentAccount;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PaymentAccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<PaymentAccount> findByPassenger(UUID passengerIdentifier) {
        return entityManager.createQuery(
            "SELECT a FROM PaymentAccount a WHERE a.passenger.uuid = :passenger ORDER BY a.createdAt",
            PaymentAccount.class
        ).setParameter("passenger", passengerIdentifier).getResultList();
    }

    public PaymentAccount findById(UUID identifier) {
        return entityManager.find(PaymentAccount.class, identifier);
    }

    @Transactional
    public PaymentAccount save(PaymentAccount account) {
        if (account.getUuid() == null) {
            entityManager.persist(account);
            return account;
        }
        return entityManager.merge(account);
    }

    @Transactional
    public void delete(UUID identifier) {
        PaymentAccount account = entityManager.find(PaymentAccount.class, identifier);
        if (account != null) {
            entityManager.remove(account);
        }
    }
}
