package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.RotationDto;
import ci.transit.system.transport.server.impl.dto.RouteDetailDto;
import ci.transit.system.transport.server.impl.dto.RouteDto;
import ci.transit.system.transport.server.impl.dto.RouteRequest;
import ci.transit.system.transport.server.impl.ennumerations.RouteStatus;
import ci.transit.system.transport.server.impl.persistence.route.Route;
import ci.transit.system.transport.server.impl.repository.RotationRepository;
import ci.transit.system.transport.server.impl.repository.RouteRepository;
import ci.transit.system.transport.server.impl.repository.RouteScheduleRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.RotationMapper;
import ci.transit.system.transport.server.impl.utilities.RouteMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RouteService {

    @Inject
    RouteRepository routeRepository;

    @Inject
    RouteScheduleRepository routeScheduleRepository;

    @Inject
    RotationRepository rotationRepository;

    public List<RouteDto> findAll(String status) {
        List<Route> routes = (status == null || status.isBlank())
            ? routeRepository.findAll()
            : routeRepository.findByStatus(parseStatus(status));
        return routes.stream().map(RouteMapper::toDto).collect(Collectors.toList());
    }

    public RouteDetailDto findById(UUID identifier) {
        Route route = requireRoute(identifier);
        RouteDetailDto dto = new RouteDetailDto();
        dto.route = RouteMapper.toDto(route);
        dto.schedules = routeScheduleRepository.findByRoute(identifier)
            .stream().map(RouteMapper::toDto).collect(Collectors.toList());
        return dto;
    }

    public List<RotationDto> findRotations(UUID identifier) {
        requireRoute(identifier);
        return rotationRepository.findByRoute(identifier)
            .stream().map(RotationMapper::toDto).collect(Collectors.toList());
    }

    public RouteDto create(RouteRequest request) {
        if (routeRepository.findByCode(request.code.trim().toUpperCase()).isPresent()) {
            throw ApiException.badRequest("Ce code de ligne est deja utilise");
        }
        Route route = new Route();
        apply(route, request);
        return RouteMapper.toDto(routeRepository.save(route));
    }

    public RouteDto update(UUID identifier, RouteRequest request) {
        Route route = requireRoute(identifier);
        routeRepository.findByCode(request.code.trim().toUpperCase()).ifPresent(other -> {
            if (!other.getUuid().equals(identifier)) {
                throw ApiException.badRequest("Ce code de ligne est deja utilise");
            }
        });
        apply(route, request);
        route.setUpdatedAt(Instant.now());
        return RouteMapper.toDto(routeRepository.save(route));
    }

    public RouteDto updateStatus(UUID identifier, String status) {
        Route route = requireRoute(identifier);
        route.setStatus(parseStatus(status));
        route.setUpdatedAt(Instant.now());
        return RouteMapper.toDto(routeRepository.save(route));
    }

    public void delete(UUID identifier) {
        requireRoute(identifier);
        if (!rotationRepository.findByRoute(identifier).isEmpty()) {
            throw ApiException.badRequest("Impossible de supprimer une ligne utilisee par des rotations");
        }
        routeRepository.delete(identifier);
    }

    Route requireRoute(UUID identifier) {
        Route route = routeRepository.findById(identifier);
        if (route == null) {
            throw ApiException.notFound("Ligne introuvable");
        }
        return route;
    }

    private void apply(Route route, RouteRequest request) {
        route.setCode(request.code.trim().toUpperCase());
        route.setName(request.name.trim());
        route.setOrigin(request.origin.trim());
        route.setDestination(request.destination.trim());
        route.setDistanceKm(request.distanceKm);
        if (request.status != null && !request.status.isBlank()) {
            route.setStatus(parseStatus(request.status));
        }
    }

    private RouteStatus parseStatus(String value) {
        try {
            return RouteStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut de ligne invalide : " + value);
        }
    }
}
