package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.route.RouteSchedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RouteScheduleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<RouteSchedule> findByRoute(UUID routeIdentifier) {
        return entityManager.createQuery(
            "SELECT s FROM RouteSchedule s WHERE s.route.uuid = :route "
          + "ORDER BY s.dayOfWeek, s.departureTime", RouteSchedule.class
        ).setParameter("route", routeIdentifier).getResultList();
    }

    public RouteSchedule findById(UUID identifier) {
        return entityManager.find(RouteSchedule.class, identifier);
    }

    @Transactional
    public RouteSchedule save(RouteSchedule schedule) {
        if (schedule.getUuid() == null) {
            entityManager.persist(schedule);
            return schedule;
        }
        return entityManager.merge(schedule);
    }

    @Transactional
    public void delete(UUID identifier) {
        RouteSchedule schedule = entityManager.find(RouteSchedule.class, identifier);
        if (schedule != null) {
            entityManager.remove(schedule);
        }
    }
}
