package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.transit.system.transport.server.impl.ennumerations.RouteStatus;
import ci.transit.system.transport.server.impl.persistence.route.Route;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RouteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Route> findAll() {
        return entityManager.createQuery(
            "SELECT r FROM Route r ORDER BY r.code", Route.class
        ).getResultList();
    }

    public List<Route> findByStatus(RouteStatus status) {
        return entityManager.createQuery(
            "SELECT r FROM Route r WHERE r.status = :status ORDER BY r.code", Route.class
        ).setParameter("status", status).getResultList();
    }

    public Optional<Route> findByCode(String code) {
        try {
            return Optional.of(entityManager.createQuery(
                "SELECT r FROM Route r WHERE r.code = :code", Route.class
            ).setParameter("code", code).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Route findById(UUID identifier) {
        return entityManager.find(Route.class, identifier);
    }

    @Transactional
    public Route save(Route route) {
        if (route.getUuid() == null) {
            entityManager.persist(route);
            return route;
        }
        return entityManager.merge(route);
    }

    @Transactional
    public void delete(UUID identifier) {
        Route route = entityManager.find(Route.class, identifier);
        if (route != null) {
            entityManager.remove(route);
        }
    }
}
