package ci.transit.system.transport.server.impl.resource;

import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.RouteScheduleDto;
import ci.transit.system.transport.server.impl.dto.RouteScheduleRequest;
import ci.transit.system.transport.server.impl.service.RouteScheduleService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/route-schedules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class RouteScheduleResource {

    @Inject
    RouteScheduleService routeScheduleService;

    @PUT
    @Path("/{id}")
    public RouteScheduleDto updateSchedule(@PathParam("id") UUID identifier, @Valid RouteScheduleRequest request) {
        return routeScheduleService.update(identifier, request);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSchedule(@PathParam("id") UUID identifier) {
        routeScheduleService.delete(identifier);
        return Response.noContent().build();
    }
}
