package ci.transit.system.transport.server.impl.persistence.route;

import java.time.LocalTime;

import ci.transit.system.transport.server.impl.ennumerations.ScheduleDay;
import ci.transit.system.transport.server.impl.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Cette classe represente l'entite des horaires recurrents d'une ligne.
 *
 * @author Transit
 *
 */
@Getter
@Setter
@Entity
@Table(name = "route_schedules")
public class RouteSchedule extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private ScheduleDay dayOfWeek;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;
}
