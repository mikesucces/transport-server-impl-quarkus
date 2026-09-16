package ci.transit.system.transport.server.impl.repository;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.persistence.attendance.Attendance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AttendanceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Attendance> findAll() {
        return entityManager.createQuery(
            "SELECT a FROM Attendance a ORDER BY a.boardedAt DESC", Attendance.class
        ).getResultList();
    }

    public List<Attendance> findByRotation(UUID rotationIdentifier) {
        return entityManager.createQuery(
            "SELECT a FROM Attendance a WHERE a.rotation.uuid = :rotation ORDER BY a.boardedAt DESC",
            Attendance.class
        ).setParameter("rotation", rotationIdentifier).getResultList();
    }

    public Attendance findById(UUID identifier) {
        return entityManager.find(Attendance.class, identifier);
    }

    @Transactional
    public Attendance save(Attendance attendance) {
        if (attendance.getUuid() == null) {
            entityManager.persist(attendance);
            return attendance;
        }
        return entityManager.merge(attendance);
    }

    @Transactional
    public void delete(UUID identifier) {
        Attendance attendance = entityManager.find(Attendance.class, identifier);
        if (attendance != null) {
            entityManager.remove(attendance);
        }
    }
}
