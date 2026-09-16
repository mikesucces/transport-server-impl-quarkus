package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.RouteDto;
import ci.transit.system.transport.server.impl.dto.RouteScheduleDto;
import ci.transit.system.transport.server.impl.persistence.route.Route;
import ci.transit.system.transport.server.impl.persistence.route.RouteSchedule;

/**
 * Cette classe convertit les entites du module Lignes en DTOs.
 *
 * @author Transit
 *
 */
public final class RouteMapper {

    private RouteMapper() {
    }

    public static RouteDto toDto(Route entity) {
        RouteDto dto = new RouteDto();
        dto.identifier = entity.getUuid();
        dto.code = entity.getCode();
        dto.name = entity.getName();
        dto.origin = entity.getOrigin();
        dto.destination = entity.getDestination();
        dto.distanceKm = entity.getDistanceKm();
        dto.status = entity.getStatus().name();
        return dto;
    }

    public static RouteScheduleDto toDto(RouteSchedule entity) {
        RouteScheduleDto dto = new RouteScheduleDto();
        dto.identifier = entity.getUuid();
        dto.routeIdentifier = entity.getRoute().getUuid();
        dto.dayOfWeek = entity.getDayOfWeek().name();
        dto.departureTime = entity.getDepartureTime();
        return dto;
    }
}
