package ci.transit.system.transport.server.impl.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.RouteScheduleDto;
import ci.transit.system.transport.server.impl.dto.RouteScheduleRequest;
import ci.transit.system.transport.server.impl.ennumerations.ScheduleDay;
import ci.transit.system.transport.server.impl.persistence.route.RouteSchedule;
import ci.transit.system.transport.server.impl.repository.RouteScheduleRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.RouteMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RouteScheduleService {

    @Inject
    RouteScheduleRepository routeScheduleRepository;

    @Inject
    RouteService routeService;

    public List<RouteScheduleDto> findByRoute(UUID routeIdentifier) {
        routeService.requireRoute(routeIdentifier);
        return routeScheduleRepository.findByRoute(routeIdentifier)
            .stream().map(RouteMapper::toDto).collect(Collectors.toList());
    }

    public RouteScheduleDto create(UUID routeIdentifier, RouteScheduleRequest request) {
        RouteSchedule schedule = new RouteSchedule();
        schedule.setRoute(routeService.requireRoute(routeIdentifier));
        apply(schedule, request);
        return RouteMapper.toDto(routeScheduleRepository.save(schedule));
    }

    public RouteScheduleDto update(UUID identifier, RouteScheduleRequest request) {
        RouteSchedule schedule = requireSchedule(identifier);
        apply(schedule, request);
        return RouteMapper.toDto(routeScheduleRepository.save(schedule));
    }

    public void delete(UUID identifier) {
        requireSchedule(identifier);
        routeScheduleRepository.delete(identifier);
    }

    private RouteSchedule requireSchedule(UUID identifier) {
        RouteSchedule schedule = routeScheduleRepository.findById(identifier);
        if (schedule == null) {
            throw ApiException.notFound("Horaire introuvable");
        }
        return schedule;
    }

    private void apply(RouteSchedule schedule, RouteScheduleRequest request) {
        try {
            schedule.setDayOfWeek(ScheduleDay.valueOf(request.dayOfWeek.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Jour de la semaine invalide : " + request.dayOfWeek);
        }
        schedule.setDepartureTime(request.departureTime);
    }
}
