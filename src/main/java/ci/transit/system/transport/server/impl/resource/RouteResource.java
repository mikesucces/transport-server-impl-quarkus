package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.RotationDto;
import ci.transit.system.transport.server.impl.dto.RouteDetailDto;
import ci.transit.system.transport.server.impl.dto.RouteDto;
import ci.transit.system.transport.server.impl.dto.RouteRequest;
import ci.transit.system.transport.server.impl.dto.RouteScheduleDto;
import ci.transit.system.transport.server.impl.dto.RouteScheduleRequest;
import ci.transit.system.transport.server.impl.service.RouteScheduleService;
import ci.transit.system.transport.server.impl.service.RouteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouteResource {

    @Inject
    RouteService routeService;

    @Inject
    RouteScheduleService routeScheduleService;

    @GET
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER", "DRIVER"})
    public List<RouteDto> getAllRoutes(@QueryParam("status") String status) {
        return routeService.findAll(status);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER", "CONTROLLER", "DRIVER"})
    public RouteDetailDto getRoute(@PathParam("id") UUID identifier) {
        return routeService.findById(identifier);
    }

    @POST
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createRoute(@Valid RouteRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(routeService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "MANAGER"})
    public RouteDto updateRoute(@PathParam("id") UUID identifier, @Valid RouteRequest request) {
        return routeService.update(identifier, request);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"OWNER", "MANAGER"})
    public RouteDto updateStatus(@PathParam("id") UUID identifier, Map<String, String> body) {
        return routeService.updateStatus(identifier, body.get("status"));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("OWNER")
    public Response deleteRoute(@PathParam("id") UUID identifier) {
        routeService.delete(identifier);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/schedules")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<RouteScheduleDto> getSchedules(@PathParam("id") UUID identifier) {
        return routeScheduleService.findByRoute(identifier);
    }

    @POST
    @Path("/{id}/schedules")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response createSchedule(@PathParam("id") UUID identifier, @Valid RouteScheduleRequest request) {
        return Response.status(Response.Status.CREATED)
            .entity(routeScheduleService.create(identifier, request)).build();
    }

    @GET
    @Path("/{id}/rotations")
    @RolesAllowed({"OWNER", "MANAGER"})
    public List<RotationDto> getRotations(@PathParam("id") UUID identifier) {
        return routeService.findRotations(identifier);
    }
}
