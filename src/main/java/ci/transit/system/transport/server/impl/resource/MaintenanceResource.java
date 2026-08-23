package ci.transit.system.transport.server.impl.resource;

import java.util.List;
import java.util.UUID;

import ci.transit.system.transport.server.impl.dto.MaintenanceDto;
import ci.transit.system.transport.server.impl.dto.MaintenanceRequest;
import ci.transit.system.transport.server.impl.service.MaintenanceService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/maintenances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"OWNER", "MANAGER"})
public class MaintenanceResource {

    @Inject
    MaintenanceService maintenanceService;

    @GET
    @Path("/upcoming")
    public List<MaintenanceDto> getUpcomingMaintenances(
            @QueryParam("days") @DefaultValue("30") int days) {
        return maintenanceService.findUpcoming(days);
    }

    @PUT
    @Path("/{id}")
    public MaintenanceDto updateMaintenance(@PathParam("id") UUID identifier,
                                            @Valid MaintenanceRequest request) {
        return maintenanceService.update(identifier, request);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMaintenance(@PathParam("id") UUID identifier) {
        maintenanceService.delete(identifier);
        return Response.noContent().build();
    }
}
